# Streaming HTTP to HTTP

This sample will show how you can set up the EDC to stream messages from HTTP to HTTP.
This code is only for demonstration purposes and should not be used in production.

## Concept
We will build a data-plane `DataSource` extension that will retrieve new data from a disk folder and push it
to every consumer that has started a `TransferProcess` for a related asset.

### Run

Build the connector runtime, which will be used both for the provider and consumer:
```shell
./gradlew :transfer:streaming:streaming-01-http-to-http:streaming-01-runtime:build
```

Run the provider and the consumer, which must be started from different terminal shells:
```shell
# provider
export EDC_FS_CONFIG=transfer/streaming/streaming-01-http-to-http/streaming-01-runtime/provider.properties
java -jar transfer/streaming/streaming-01-http-to-http/streaming-01-runtime/build/libs/connector.jar

#consumer
export EDC_FS_CONFIG=transfer/streaming/streaming-01-http-to-http/streaming-01-runtime/consumer.properties
java -jar transfer/streaming/streaming-01-http-to-http/streaming-01-runtime/build/libs/connector.jar
```

#### Register Data Plane on provider
The provider connector needs to be aware of the streaming capabilities of the embedded dataplane, which can be registered with 
this call:
```js
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-01-http-to-http/dataplane.json  -X POST "http://localhost:18181/management/v2/dataplanes"
```

If you look at the `dataplane.json` you'll notice that the supported source is `HttpStreaming` and the supported sink is `HttpData`.

#### Register Asset, Policy Definition and Contract Definition on provider
A "source" folder must first be created where the data plane will get the messages to be sent to the consumers.
To do this, create a temp folder:
```shell
mkdir /tmp/source
```

Then put the path in the [asset.json](asset.json) file replacing the `{{sourceFolder}}` placeholder.
```json
  "dataAddress": {
    "type": "HttpStreaming",
    "sourceFolder": "{{sourceFolder}}"
  }
```

Then create the Asset, the Policy Definition and the Contract Definition with these three calls:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-01-http-to-http/asset.json  -X POST "http://localhost:18181/management/v3/assets"
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-01-http-to-http/policy-definition.json  -X POST "http://localhost:18181/management/v2/policydefinitions"
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-01-http-to-http/contract-definition.json  -X POST "http://localhost:18181/management/v2/contractdefinitions"
```

#### Negotiate the contract
The typical flow requires fetching the catalog from the consumer side and using the contract offer to negotiate a contract. 
However, in this sample case, we already have the provider asset (`"stream-asset"`) so we can get the related dataset 
directly with this call:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-01-http-to-http/get-dataset.json  -X POST "http://localhost:28181/management/v2/catalog/dataset/request" -s | jq
```

The output will be something like:
```json
{
  "@id": "stream-asset",
  "@type": "dcat:Dataset",
  "odrl:hasPolicy": {
    "@id": "Y29udHJhY3QtZGVmaW5pdGlvbg==:c3RyZWFtLWFzc2V0:NDlhYTUzZWEtMDUzMS00ZDkyLTg4Y2YtMGRjMTc4MmQ1NjY4",
    "@type": "odrl:Set",
    "odrl:permission": [],
    "odrl:prohibition": [],
    "odrl:obligation": [],
    "odrl:target": "stream-asset"
  },
  "dcat:distribution": {
    "@type": "dcat:Distribution",
    "dct:format": {
      "@id": "HttpData"
    },
    "dcat:accessService": "b24dfdbc-d17f-4d6e-9b5c-8fa71dacecfc"
  },
  "edc:id": "stream-asset",
  "@context": {
    "dct": "https://purl.org/dc/terms/",
    "edc": "https://w3id.org/edc/v0.0.1/ns/",
    "dcat": "https://www.w3.org/ns/dcat/",
    "odrl": "http://www.w3.org/ns/odrl/2/",
    "dspace": "https://w3id.org/dspace/v0.8/"
  }
}
```

With the `odrl:hasPolicy/@id` we can now replace it in the [negotiate-contract.json](negotiate-contract.json) file
and request the contract negotiation:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-01-http-to-http/negotiate-contract.json  -X POST "http://localhost:28181/management/v2/contractnegotiations" -s | jq
```

### Start the transfer
First we need to set up the receiver server on the consumer side that will receive a call for every message. For this
you'll need to open another terminal shell and run:
```shell
./gradlew util:http-request-logger:build
HTTP_SERVER_PORT=4000 java -jar util/http-request-logger/build/libs/http-request-logger.jar
```
It will run on port 4000.

At this point the contract agreement should already been issued, to verify that, please check the contract negotiation state with
this call, replacing `{{contract-negotiation-id}}` with the id returned by the negotiate contract call.
```shell
curl "http://localhost:28181/management/v2/contractnegotiations/{{contract-negotiation-id}}" -s | jq
```

If the `edc:contractAgreementId` is valued, it can be used to start the transfer, replacing it in the [transfer.json](transfer.json)
file to `{{contract-agreement-id}}` and then calling the connector with this command:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-01-http-to-http/transfer.json  -X POST "http://localhost:28181/management/v2/transferprocesses" -s | jq
```
> Note that the destination address is `localhost:4000`, this because is where our http server is listening.


Let's wait until the transfer state is `STARTED` state executing this call, replacing to `{{transfer-process-id}}` the id returned
by the start transfer call:
```shell
curl "http://localhost:28181/management/v2/transferprocesses/{{transfer-process-id}}" -s | jq
```

Here we can test the transfer creating a file into the `source` folder that we configured before, e.g. copying the `README.md`
into the `source` folder:
```shell
cp README.md /tmp/source
```

we should see the content logged into the received server:
```
Incoming request
Method: POST
Path: /
Body:
# EDC Samples
...
```
### Up to you: second connector

As a challenge, try starting another consumer connector, negotiating a contract, and starting the transfer.
Every message pushed by the provider will be sent to all the consumers.

## Technical insight

The required code is contained in the [`streaming-01-runtime` source folder](transfer/streaming/streaming-01-http-to-http/streaming-01-runtime/src/main/java/org/eclipse/edc/samples/transfer/streaming/http).
