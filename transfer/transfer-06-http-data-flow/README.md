# Implement a simple Http Data Flow

# How to build a connector
Consumer and Provider are both connectors and before to start them, we need to
build the connector module as well with the following command
```bash
./gradlew transfer/transfer-06-http-data-flow/connector/build
```

After the build end you should verify that the connector jar is created in the directory
[connector.jar file](./transfer/transfer-06-http-data-flow/connector/build/libs/connector.jar)

# How to run a connector

### Run a provider
To run a provider you should run the following command

```bash
java -Dedc.vault=transfer/transfer-06-http-data-flow/provider/provider-vault.properties -Dedc.keystore=transfer/transfer-06-http-data-flow/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.fs.config=transfer/transfer-06-http-data-flow/provider/provider-configuration.properties -jar transfer/transfer-06-http-data-flow/connector/build/libs/connector.jar
```

### Run a consumer
To run a consumer you should run the following command

```bash
java -Dedc.vault=transfer/transfer-06-http-data-flow/consumer/consumer-vault.properties -Dedc.keystore=transfer/transfer-06-http-data-flow/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.fs.config=transfer/transfer-06-http-data-flow/consumer/consumer-configuration.properties -jar transfer/transfer-06-http-data-flow/connector/build/libs/connector.jar

```

### Register data plane instance for provider
```bash
curl -H 'Content-Type: application/json' \
     -d '{
   "edctype": "dataspaceconnector:dataplaneinstance",
   "id": "provider-dataplane",
   "url": "http://localhost:19292/control/transfer",
   "allowedSourceTypes": [ "HttpData" ],
   "allowedDestTypes": [ "HttpProxy", "HttpData" ],
   "properties": {
     "publicApiUrl": "http://localhost:19291/public/"
   }
 }' \
     -X POST "http://localhost:19195/dataplane/instances"
```

### Register data plane instance for consumer
```bash
curl -H 'Content-Type: application/json' \
     -d '{
   "edctype": "dataspaceconnector:dataplaneinstance",
   "id": "consumer-dataplane",
   "url": "http://localhost:29292/control/transfer",
   "allowedSourceTypes": [ "HttpData" ],
   "allowedDestTypes": [ "HttpProxy", "HttpData" ],
   "properties": {
     "publicApiUrl": "http://localhost:29291/public/"
   }
 }' \
     -X POST "http://localhost:29195/dataplane/instances"
```

### Create an Asset on the provider side
```bash
curl -d '{
           "asset": {
             "properties": {
               "asset:prop:id": "assetId",
               "asset:prop:name": "product description",
               "asset:prop:contenttype": "application/json"
             }
           },
           "dataAddress": {
             "properties": {
               "name": "Test asset",
               "baseUrl": "https://jsonplaceholder.typicode.com/users",
               "type": "HttpData"
             }
           }
         }' -H 'content-type: application/json' http://localhost:19193/api/v1/data/assets
```

### Create a Policy on the provider 
```bash
curl -d '{
           "id": "aPolicy",
           "policy": {
             "uid": "231802-bb34-11ec-8422-0242ac120002",
             "permissions": [
               {
                 "target": "assetId",
                 "action": {
                   "type": "USE"
                 },
                 "edctype": "dataspaceconnector:permission"
               }
             ],
             "@type": {
               "@policytype": "set"
             }
           }
         }' -H 'content-type: application/json' http://localhost:19193/api/v1/data/policydefinitions
```

### Create a contract definition on Provider
```bash
curl -d '{
           "id": "1",
           "accessPolicyId": "aPolicy",
           "contractPolicyId": "aPolicy",
           "criteria": []
         }' -H 'content-type: application/json' http://localhost:19193/api/v1/data/contractdefinitions
```

# How to fetch catalog on consumer side
```bash
curl http://localhost:29193/api/v1/data/catalog\?providerUrl\=http://localhost:19194/api/v1/ids/data
```

# Negotiate a contract
```bash
curl -d '{
           "connectorId": "provider",
           "connectorAddress": "http://localhost:19194/api/v1/ids/data",
           "protocol": "ids-multipart",
           "offer": {
             "offerId": "1:50f75a7a-5f81-4764-b2f9-ac258c3628e2",
             "assetId": "assetId",
             "policy": {
               "uid": "231802-bb34-11ec-8422-0242ac120002",
               "permissions": [
                 {
                   "target": "assetId",
                   "action": {
                     "type": "USE"
                   },
                   "edctype": "dataspaceconnector:permission"
                 }
               ],
               "@type": {
                 "@policytype": "set"
               }
             }
           }
         }' -X POST -H 'content-type: application/json' http://localhost:29193/api/v1/data/contractnegotiations \
         -s | jq
```
# Getting the contract agreement id
```bash
curl -X GET "http://localhost:29193/api/v1/data/contractnegotiations/<contract negotiation id, returned by the negotiation call>" \
    --header 'Content-Type: application/json' \
    -s | jq
```

# Start the transfer
```bash
curl -X POST "http://localhost:29193/api/v1/data/transferprocess" \
    --header "Content-Type: application/json" \
    --data '{
                "connectorId": "provider",
                "connectorAddress": "http://localhost:19194/api/v1/ids/data",
                "contractId": "<contract agreement id>",
                "assetId": "assetId",
                "managedResources": "false",
                "dataDestination": { "type": "HttpProxy" }
            }' \
    -s | jq
```