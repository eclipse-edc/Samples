# Serverless file transfer

Serverless mechanisms for data transfers are supported in DPF. Example of serverless implementations include Azure Data Factory, a fully managed integration service that can be controlled through a REST API.
This sample shows how to transfer file between two Azure Storage with Azure Data Factory.

TBD: more description about this sample

## Deploy cloud resources

Before we get into the serverless data transfers, we need to set up cloud resources.

Firstly, you will need to be logged in to your Azure CLI by entering the following commands in a shell:
```bash
az login
```
Then run the following script to deploy Azure resources. Resource names can be adapted by editing the script.
```bash
./transfer/transfer-08-serverless-file-transfer/shell-scripts/create-cloud-resources.sh
```
Once the DPF server is running, all resources have been deployed.
Resources include (TBD: list up all resources we deployed)
- Azure Storage Account


## Update connector config

_Do the following for both the consumer's and the provider's `config.properties`!_

Let's modify the following config values to the connector configuration `config.properties`
Consumer:
```properties
#azure key vault
edc.vault.clientid=<client_id>
edc.vault.tenantid=<tenant_id>
edc.vault.name=<vault_name>
edc.vault.clientsecret=<client_secret>
```
Provider:
```properties
#azure key vault
edc.vault.clientid=<client_id>
edc.vault.tenantid=<tenant_id>
edc.vault.name=<vault_name>
edc.vault.clientsecret=<client_secret>
#azure data factory
edc.data.factory.resource.id=<data_factory_resource_id>
edc.data.factory.key.vault.resource.id=<linked_key_value_resource_id>
#azure resource manager
edc.azure.tenant.id=<tenant_id>
edc.azure.subscription.id=<subscription_id>
```

## Update data seeder

Put the storage account name into the `DataAddress` builders within the `CloudTransferExtension` class.

```
DataAddress.Builder.newInstance()
   .type("AzureStorage")
   .property("account", "<storage-account-name>")
   .property("container", "src-container")
   .property("blobname", "test-document.txt")
   .keyName("<storage-account-name>-key1")
   .build();
```

## Bringing it all together

### 1. Boot connectors

While we have deployed several cloud resources in the previous chapter, the connectors themselves still run locally.
Thus, we can simply rebuild and run them:

```bash
./gradlew clean :transfer:transfer-08-serverless-file-transfer:serverless-transfer-consumer:build
java -Dedc.fs.config=transfer/transfer-08-serverless-file-transfer/serverless-transfer-consumer/config.properties -jar transfer/transfer-08-serverless-file-transfer/serverless-transfer-consumer/build/libs/consumer.jar
# in another terminal window:
./gradlew clean :transfer:transfer-08-serverless-file-transfer:serverless-transfer-provider:build
java -Dedc.fs.config=transfer/transfer-08-serverless-file-transfer/serverless-transfer-provider/config.properties -jar transfer/transfer-08-serverless-file-transfer/serverless-transfer-provider/build/libs/provider.jar
```

### 2. Retrieve provider Contract Offers

To request data offers from the provider, run:

```bash
curl -X POST "http://localhost:9192/api/v1/management/catalog/request" \
--header 'X-Api-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '{
  "providerUrl": "http://localhost:8282/api/v1/ids/data"
}'
```

#### 3. Negotiate Contract

To negotiate a contract copy one of the contract offers into the statement below and execute it. At the time of writing
it is only possible to negotiate an _unchanged_ contract, so counter offers are not supported.

```bash
curl --location --request POST 'http://localhost:9192/api/v1/management/contractnegotiations' \
--header 'X-API-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '{
  "connectorId": "provider",
  "connectorAddress": "http://localhost:8282/api/v1/ids/data",
  "protocol": "ids-multipart",
  "offer": {
    "offerId": "1:3a75736e-001d-4364-8bd4-9888490edb58",
    "assetId": "1",
    "policy": { <Copy the first policy from the previous response (the one with "target: 1")> }
  }
}'
```

The EDC will answer with the contract negotiation id. This id will be used in step 4.

#### 4. Get Contract Agreement Id

To get the contract agreement id insert the negotiation id into the following statement end execute it.

```bash
curl -X GET -H 'X-Api-Key: password' "http://localhost:9192/api/v1/management/contractnegotiations/{negotiationId}"
```

The EDC will return the current state of the contract negotiation. When the negotiation is completed successfully
(this may take a few seconds), the response will also contain an agreement id, that is required in the next step.

#### 5. Transfer Data

To initiate the data transfer, execute the statement below. Please take care of setting the contract agreement id
obtained at previous step as well as a unique bucket name.

```bash
curl --location --request POST 'http://localhost:9192/api/v1/management/transferprocess' \
--header 'X-API-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '
{
  "connectorAddress": "http://localhost:8282/api/v1/ids/data",
  "protocol": "ids-multipart",
  "connectorId": "consumer",
  "assetId": "1",
  "contractId": "<ContractAgreementId>",
  "dataDestination": {
      "properties": {
          "type": "AzureStorage",
          "account": "<storage-account-name>",
          "container:": "<container-name>"
      }
  },
  "managedResources": true,
  "transferType": {
    "contentType": "application/octet-stream",
    "isFinite": true
  }
}'
```

This command will return a transfer process id which will used to request the deprovisioning of the resources.

#### 6. Deprovision resources

Deprovisioning is not necessary per se, but it will do some cleanup, delete the temporary Azure Storage Account, so
it's generally advisable to do it.

```bash
curl -X POST -H 'X-Api-Key: password' "http://localhost:9192/api/v1/management/transferprocess/{transferProcessId}/deprovision"
```

Finally, run terraform to clean-up the vault and other remaining stuffs:

TBD: write script to clean up
```bash

```

---
[Previous Chapter](../transfer-07-provider-push-http/README.md)
