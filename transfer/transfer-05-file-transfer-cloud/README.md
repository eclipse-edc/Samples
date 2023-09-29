# Improve the file transfer

So far we have performed a file transfer on a local machine using the Eclipse Dataspace Connector. While that is already
great progress, it probably won't be much use in a real-world production application.

This chapter improves on this by moving the file transfer "to the cloud". What we mean by that is that instead of
reading and writing the file from/to the disk, we will now:

- read the source from an Azure Storage,
- put the destination file into an AWS S3 Bucket.

## Setup local dev environment

Before we get into the nitty-gritty of cloud-based data transfers, we need to set up cloud resources. While we could do
that manually clicking through the Azure and AWS portals, there are simply more elegant solutions around. We use
Hashicorp Terraform for deployment and maintenance.

> You will need an active Azure Subscription and an AWS Account with root-user/admin access! Both platforms offer free
> tiers, so no immediate cost incurs.

Also, you will need to be logged in to your Azure CLI as well as AWS CLI by entering the following commands in a shell:

```bash
az login
aws configure
```

The deployment scripts will provision all resources in Azure and AWS (that's why you need to be logged in to the CLIs)
and store all access credentials in an Azure Vault (learn more
[here](https://azure.microsoft.com/de-de/services/key-vault/#product-overview)).

## Deploy cloud resources

It's as simple as running the main terraform script:

```bash
cd transfer/transfer-05-file-transfer-cloud/terraform
terraform init --upgrade
terraform apply
```

it will prompt you to enter a unique name, which will serve as prefix for many resources both in Azure and in AWS. Then,
enter "yes" and let terraform works its magic.

It shouldn't take more than a couple of minutes, and when it's done it will log the `client_id`, `tenant_id`
, `vault-name`, `storage-container-name` and `storage-account-name`.
> Take a note of these values!

Download the certificate used to authenticate the generated service principal against Azure Active Directory:

```bash
terraform output -raw certificate | base64 --decode > cert.pfx
```

## Update connector config

_Do the following for both the consumer's and the provider's `config.properties`!_

Let's modify the following config values to the connector configuration `config.properties` and insert the values that
terraform logged before:

```properties
edc.vault.clientid=<client_id>
edc.vault.tenantid=<tenant_id>
edc.vault.certificate=<path_to_pfx_file>
edc.vault.name=<vault-name>
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
./gradlew clean build
java -Dedc.fs.config=transfer/transfer-05-file-transfer-cloud/cloud-transfer-consumer/config.properties -jar transfer/transfer-05-file-transfer-cloud/cloud-transfer-consumer/build/libs/consumer.jar
# in another terminal window:
java -Dedc.fs.config=transfer/transfer-05-file-transfer-cloud/cloud-transfer-provider/config.properties -jar transfer/transfer-05-file-transfer-cloud/cloud-transfer-provider/build/libs/provider.jar
```

### 2. Retrieve provider Contract Offers

To request data offers from the provider, run:

```bash
curl -X POST "http://localhost:9192/management/catalog/request" \
--header 'X-Api-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '{
  "providerUrl": "http://localhost:8282/protocol"
}'
```

#### 3. Negotiate Contract

To negotiate a contract copy one of the contract offers into the statement below and execute it. At the time of writing
it is only possible to negotiate an _unchanged_ contract, so counter offers are not supported.

```bash
curl --location --request POST 'http://localhost:9192/management/v2/contractnegotiations' \
--header 'X-API-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '{
  "connectorId": "provider",
  "connectorAddress": "http://localhost:8282/protocol",
  "protocol": "dataspace-protocol-http",
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
curl -X GET -H 'X-Api-Key: password' "http://localhost:9192/management/v2/contractnegotiations/{negotiationId}"
```

The EDC will return the current state of the contract negotiation. When the negotiation is completed successfully
(this may take a few seconds), the response will also contain an agreement id, that is required in the next step.

#### 5. Transfer Data

To initiate the data transfer, execute the statement below. Please take care of setting the contract agreement id
obtained at previous step as well as a unique bucket name.

```bash
curl --location --request POST 'http://localhost:9192/management/v2/transferprocesses' \
--header 'X-API-Key: password' \
--header 'Content-Type: application/json' \
--data-raw '
{
  "connectorAddress": "http://localhost:8282/protocol",
  "protocol": "dataspace-protocol-http",
  "connectorId": "consumer",
  "assetId": "1",
  "contractId": "<ContractAgreementId>",
  "dataDestination": {
    "type": "AmazonS3",
    "region": "us-east-1",
    "bucketName": "<Unique bucket name>"
  },
  "transferType": {
    "contentType": "application/octet-stream",
    "isFinite": true
  }
}'
```

This command will return a transfer process id which will used to request the deprovisioning of the resources.

#### 6. Deprovision resources

Deprovisioning is not necessary per se, but it will do some cleanup, delete the temporary AWS role and the S3 bucket, so
it's generally advisable to do it.

```bash
curl -X POST -H 'X-Api-Key: password' "http://localhost:9192/management/v2/transferprocesses/{transferProcessId}/deprovision"
```

Finally, run terraform to clean-up the vault and other remaining stuffs:

```bash
cd transfer/transfer-05-file-transfer-cloud/terraform
terraform destroy
```

---

[Previous Chapter](../transfer-04-open-telemetry/README.md)
