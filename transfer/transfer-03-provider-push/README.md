# Implement a simple "Provider Push" Http transfer flow

This sample demonstrates the "provider push" use case that you can find more details
on [Transfer data plane documentation](https://github.com/eclipse-edc/Connector/tree/main/extensions/control-plane/transfer/transfer-data-plane).

This samples consists of:

* Performing a file transfer
    * The consumer will initiate a file transfer
    * Provider Control Plane retrieves the DataAddress of the actual data source and creates a
      DataFlowRequest based on the received DataRequest and this data address
* Provider Data Plane fetches data from the actual data source
* Provider Data Plane pushes data to the consumer service

## Prerequisites

For the "provider push" use case the "edc.receiver.http.endpoint" is not needed.
Terminate your current consumer connector. 

Execute this to re-run the consumer connector with proper configuration:

```bash
java -Dedc.keystore=transfer/transfer-00-prerequisites/resources/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.vault=transfer/transfer-00-prerequisites/resources/configuration/consumer-vault.properties -Dedc.fs.config=transfer/transfer-03-provider-push/configuration/consumer-configuration.properties -jar transfer/transfer-00-prerequisites/connector/build/libs/connector.jar
```

Don't forget to register the data plane for the consumer since it has been started again.

```bash
curl -H 'Content-Type: application/json' \
     -d '{
           "@context": {
             "edc": "https://w3id.org/edc/v0.0.1/ns/"
           },
           "@id": "http-pull-consumer-dataplane",
           "url": "http://localhost:29192/control/transfer",
           "allowedSourceTypes": [ "HttpData" ],
           "allowedDestTypes": [ "HttpProxy", "HttpData" ],
           "properties": {
             "https://w3id.org/edc/v0.0.1/ns/publicApiUrl/publicApiUrl": "http://localhost:29291/public/"
            }
         }' \
             -X POST "http://localhost:29193/management/v2/dataplanes"
```

You can leave your provider connector from before running.
Also make sure the http server from the [Consumer Pull](../transfer-02-consumer-pull/README.md) chapter is still running.
Restart it otherwise.

# Run the sample

Running this sample consists of multiple steps, that are executed one by one and following the same
order.

### 1. Start the transfer

Before executing the request, insert the contract agreement ID from the [Negotiation](../transfer-01-negotiation/README.md)
chapter. We will re-use the same asset, policies and contract negotiation from before.

```bash
curl -X POST "http://localhost:29193/management/v2/transferprocesses" \
    -H "Content-Type: application/json" \
    -d '{
        "@context": {
          "edc": "https://w3id.org/edc/v0.0.1/ns/"
        },
        "@type": "TransferRequestDto",
        "connectorId": "provider",
        "connectorAddress": "http://localhost:19194/protocol",
        "contractId": "<contract agreement id>",
        "assetId": "assetId",
        "protocol": "dataspace-protocol-http",
        "dataDestination": { 
          "type": "HttpData",
          "baseUrl": "http://localhost:4000/api/consumer/store"
        }
    }' \
    -s | jq
```
> keep in mind that, to make a transfer with a provider push method, the dataDestination type should
> be any value different from the "HttpProxy".

Sample output:

```json
 {
  ...
  "@id": "591bb609-1edb-4a6b-babe-50f1eca3e1e9",
  "edc:createdAt": 1674078357807,
  ...
}
```

### 2. Check the transfer status

Due to the nature of the transfer, it will be very fast and most likely already done by the time you
read the UUID.

```bash
curl http://localhost:29193/management/v2/transferprocesses/<transfer process id>
```

### 3. Check the data

At this step, you can check the data by checking the log of the http server exposed on port 4000, you should see a log
that shows the same data that you can get from https://jsonplaceholder.typicode.com/users.

[Next Chapter](../transfer-04-event-consumer/README.md)