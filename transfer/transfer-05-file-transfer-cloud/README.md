# Improve the file transfer

So far, we have performed a file transfer on a local machine using the Eclipse Dataspace Connector. While that is already great progress, it probably won't be much use in a real-world production application.

This chapter improves on this by shifting the file transfer between cloud storage emulators. We will now:

- read the source from an Azurite instance,
- put the destination file into a MinIO instance.

## Prerequisites

The following steps assume that you have Docker, Vault and the Azure CLI installed. If this is not the case, you can use the following links to access the installation instructions for all three.

- Docker: https://docs.docker.com/engine/install/
- Vault: https://developer.hashicorp.com/vault/docs/install
- Azure CLI: https://learn.microsoft.com/en-us/cli/azure/install-azure-cli

## Start the docker-compose file

```bash
docker compose -f transfer/transfer-05-file-transfer-cloud/resources/docker-compose.yaml up -d
```

Please check in the logs that minio, azurite and hashicorp-vault have started correctly.  

## Create bucket in minio

Go to http://localhost:9001 and login with the credentials which you can find in the [docker-compose](resources/docker-compose.yaml) file (line 20-21), then go to 'Buckets' and create a bucket with the name “src-bucket”.

## Upload file to azurite
Let`s create a container with the following commands:

```bash
conn_str="DefaultEndpointsProtocol=http;AccountName=provider;AccountKey=password;BlobEndpoint=http://127.0.0.1:10000/provider;"
az storage container create --name src-container --connection-string $conn_str
```

If the container is created successfully, you will get this:
```json
{
  "created": true
}
```

Upload the file to the blob storage:

```bash
az storage blob upload -f ./transfer/transfer-05-file-transfer-cloud/resources/test-document.txt --container-name src-container --name test-document.txt --connection-string $conn_str
```

You can run the following command to check if the file was added successfully

```bash
az storage blob list --container-name src-container --connection-string "DefaultEndpointsProtocol=http;AccountName=provider;AccountKey=password;BlobEndpoint=http://127.0.0.1:10000/provider;" --query "[].{name:name}" --output table
```

You should see the test-document.txt file.

```sh
Name
--------------------------
test-document.txt
```

## Configure the vault
We already started the vault at the beginning with docker compose. Now the following commands must be executed in a terminal window to add the necessary secrets.

```bash
export VAULT_ADDR='http://0.0.0.0:8200'
vault kv put secret/accessKeyId content=consumer
vault kv put secret/secretAccessKey content=password
vault kv put secret/provider-key content=password
```

## Bringing it all together

### 1. Boot connectors

```bash
./gradlew clean build
java -Dedc.fs.config=transfer/transfer-05-file-transfer-cloud/cloud-transfer-provider/config.properties -jar transfer/transfer-05-file-transfer-cloud/cloud-transfer-provider/build/libs/provider.jar
# in another terminal window:
java -Dedc.fs.config=transfer/transfer-05-file-transfer-cloud/cloud-transfer-consumer/config.properties -jar transfer/transfer-05-file-transfer-cloud/cloud-transfer-consumer/build/libs/consumer.jar
```


### 2. Retrieve provider Contract Offers

```bash
curl -X POST "http://localhost:29193/management/v3/catalog/request" \
    -H 'X-Api-Key: password' -H 'Content-Type: application/json' \
    -d @transfer/transfer-05-file-transfer-cloud/resources/fetch-catalog.json -s | jq
```

Please replace the {{contract-offer-id}} placeholder in the [negotiate-contract.json](resources/negotiate-contract.json) file with the contract offer id you found in the catalog at the path dcat:dataset.odrl:hasPolicy.@id (the asset with "@id: 1").

### 3. Negotiate Contract

```bash
curl -d @transfer/transfer-05-file-transfer-cloud/resources/negotiate-contract.json \
  -H 'X-Api-Key: password' X POST -H 'content-type: application/json' http://localhost:29193/management/v3/contractnegotiations \
  -s | jq
```

We can now use the UUID to check the current status of the negotiation using an endpoint on the consumer side.

### 4. Get Contract Agreement Id

```bash
curl -X GET "http://localhost:29193/management/v3/contractnegotiations/{{contract-negotiation-id}}" \
    -H 'X-Api-Key: password' --header 'Content-Type: application/json' \
    -s | jq
```

Please replace the {{contract-agreement-id}} placeholder in the [start-transfer.json](resources/start-transfer.json) file with the contractAgreementId from the previous response.

### 5. Transfer Data

```bash
curl -X POST "http://localhost:29193/management/v3/transferprocesses" \
  -H 'X-Api-Key: password' -H "Content-Type: application/json" \
  -d @transfer/transfer-05-file-transfer-cloud/resources/start-transfer.json \
  -s | jq
```

With the given UUID, we can check the transfer process.

### 6. Check Transfer Status

```bash
curl -H 'X-Api-Key: password' http://localhost:29193/management/v3/transferprocesses/<transfer-process-id> -s | jq
```


## Stop docker container
Execute the following command in a terminal window to stop the docker container:
```bash
docker compose -f transfer/transfer-05-file-transfer-cloud/resources/docker-compose.yaml down
```


---

[Previous Chapter](../transfer-04-event-consumer/README.md)