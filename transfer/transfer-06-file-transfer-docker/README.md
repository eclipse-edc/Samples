# Improve the file transfer

The previous transfer from the previous chapter was a transfer in the cloud. 
So that this can also be done without cloud accounts, a transfer between two docker images is carried out in this chapter.
Instead of a transfer from Azure Storage to an AWS S3 Bucket, we will:

- read the source from Azurite,
- put the destination file into Minio.


## Start the docker-compose file

```bash
docker-compose -f transfer/transfer-06-file-transfer-docker/resources/docker-compose.yml up -d
```

You should see this:
```sh
 ✔ Container azurite          Started 
 ✔ Container minio            Started 
```

## Create bucket in minio

Go to http://localhost:9001 and login with the credentials which you can find in the [docker-compose](resources/docker-compose.yaml) file (line 23-24), then go to 'Buckets' and create a bucket with the name “src-bucket”.

## Upload file to azurite
Before we upload the file, you have to install Amazon CLI. After that, you have to create a blob storage:

```bash
conn_str="DefaultEndpointsProtocol=http;AccountName=provider;AccountKey=password;BlobEndpoint=http://127.0.0.1:10000/provider;"
az storage container create --name src-container --connection-string $conn_str
```

If the storage is created successfully, you will get something like this:
```json
{
  "created": true
}
```

Upload the file to the blob storage:

```bash
az storage blob upload -f ./transfer/transfer-06-file-transfer-docker/resources/test-document.txt --container-name src-container --name test-document.txt --connection-string $conn_str
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


## Start the vault
Now that we have both uploaded the file on azurite and created the bucket on minio, we can install hashicorp-vault. After installation, you can start the vault server:

```bash
vault server -dev
```
Now on another terminal window:
```bash
export VAULT_ADDR='http://127.0.0.1:8200'
vault kv put secret/accessKeyId content=consumer
vault kv put secret/secretAccessKey content=password
vault kv put secret/provider-key content=password
```


## Update connector config

_Do the following for both the consumer's and the provider's `config.properties`!_

Let's modify the following config values to the connector configuration `config.properties` and insert the root-token that vault issued when we started the server:

```properties
edc.vault.hashicorp.token=<root-token>
```

## Bringing it all together

### 1. Boot connectors

```bash
./gradlew clean build
java -Dedc.fs.config=transfer/transfer-06-file-transfer-docker/docker-transfer-provider/config.properties -jar transfer/transfer-06-file-transfer-docker/docker-transfer-provider/build/libs/provider.jar
# in another terminal window:
java -Dedc.fs.config=transfer/transfer-06-file-transfer-docker/docker-transfer-consumer/config.properties -jar transfer/transfer-06-file-transfer-docker/docker-transfer-consumer/build/libs/consumer.jar
```

### 2. Create a Asset on the provider

```bash
curl -d @transfer/transfer-06-file-transfer-docker/resources/create-asset.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets \
  -s | jq
```

### 3. Create a Policy on the provider

```bash
curl -d @transfer/transfer-06-file-transfer-docker/resources/create-policy.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/policydefinitions \
  -s | jq
```

### 4. Create a contract definition on Provider

```bash
curl -d @transfer/transfer-06-file-transfer-docker/resources/create-contract-definition.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/contractdefinitions \
  -s | jq
```

### 5. Fetch catalog on consumer side

```bash
curl -X POST "http://localhost:29193/management/v3/catalog/request" \
    -H 'Content-Type: application/json' \
    -d @transfer/transfer-06-file-transfer-docker/resources/fetch-catalog.json -s | jq
```

Please replace the {{contract-offer-id}} placeholder in the [negotiate-contract.json](resources/negotiate-contract.json) file with the contract offer id you found in the catalog at the path dcat:dataset.odrl:hasPolicy.@id.

### 6. Negotiate a contract

```bash
curl -d @transfer/transfer-06-file-transfer-docker/resources/negotiate-contract.json \
  -X POST -H 'content-type: application/json' http://localhost:29193/management/v3/contractnegotiations \
  -s | jq
```

We can now use the UUID to check the current status of the negotiation using an endpoint on the consumer side.

### 7. Getting the contract Agreement id

```bash
curl -X GET "http://localhost:29193/management/v3/contractnegotiations/{{contract-negotiation-id}}" \
    --header 'Content-Type: application/json' \
    -s | jq
```

Please replace the {{contract-agreement-id}} placeholder in the [start-transfer.json](resources/start-transfer.json) file with the contractAgreementId from the previous response.

### 8. Start the transfer

```bash
curl -X POST "http://localhost:29193/management/v3/transferprocesses" \
  -H "Content-Type: application/json" \
  -d @transfer/transfer-06-file-transfer-docker/resources/start-transfer.json \
  -s | jq
```

With the given UUID, we can check the transfer process.

### 9. Check the transfer status

```bash
curl http://localhost:29193/management/v3/transferprocesses/<transfer-process-id> -s | jq
```

---

[Previous Chapter](../transfer-04-open-telemetry/README.md)
