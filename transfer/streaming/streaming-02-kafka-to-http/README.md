# Streaming KAFKA to HTTP

This sample demonstrates how to set up the EDC to stream messages from Kafka to HTTP.
This code is only for demonstration purposes and should not be used in production.

## Concept

We will use the data-plane kafka `DataSource` extension that will pull event records from a kafka topic and push it
to every consumer that has started a `TransferProcess` for a related asset.

### Run

Build the connector runtime, which will be used both for the provider and consumer:
```shell
./gradlew :transfer:streaming:streaming-02-kafka-to-http:streaming-02-runtime:build
```

Run the provider and the consumer with their own configuration, which will need to be started from different terminals:

```shell
export EDC_FS_CONFIG=transfer/streaming/streaming-02-kafka-to-http/streaming-02-runtime/provider.properties
java -jar transfer/streaming/streaming-02-kafka-to-http/streaming-02-runtime/build/libs/connector.jar
```

```shell
export EDC_FS_CONFIG=transfer/streaming/streaming-02-kafka-to-http/streaming-02-runtime/consumer.properties
java -jar transfer/streaming/streaming-02-kafka-to-http/streaming-02-runtime/build/libs/connector.jar
```

### Register Data Plane on provider

The provider connector needs to be aware of the kafka streaming capabilities of the embedded dataplane, which can be registered with 
this call:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-02-kafka-to-http/0-dataplane.json -X POST "http://localhost:18181/management/v2/dataplanes" -s | jq
```

If you look at the `0-dataplane.json` you'll notice that the supported source is `Kafka` and the supported sink is `HttpData`.

### Register Asset, Policy Definition and Contract Definition on provider

A "source" kafka topic must first be created where the data plane will get the event records to be sent to the consumers.
To do this, initiate a Kafka server with the source topic:
```shell
docker run --rm --name=kafka-kraft -e "KAFKA_CREATE_TOPICS={{topic}}:1:1" -p 9092:9092 -d bashj79/kafka-kraft:3.0.0
```

Then put values of `kafka.bootstrap.servers` and `topic` in the [1-asset.json](1-asset.json) file replacing their placeholders.
```json
{
  "dataAddress": {
    "type": "Kafka",
    "kafka.bootstrap.servers": "localhost:9092",
    "topic": "kafka-stream-topic"
  }
}
```

Then use these three calls to create the Asset, the Policy Definition and the Contract Definition:

```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-02-kafka-to-http/1-asset.json -X POST "http://localhost:18181/management/v3/assets" -s | jq
```

```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-02-kafka-to-http/2-policy-definition.json -X POST "http://localhost:18181/management/v2/policydefinitions" -s | jq
```

```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-02-kafka-to-http/3-contract-definition.json -X POST "http://localhost:18181/management/v2/contractdefinitions" -s | jq
```

### Negotiate the contract

The typical flow requires fetching the catalog from the consumer side and using the contract offer to negotiate a contract. 
However, in this sample case, we already have the provider asset (`"kafka-stream-asset"`) so we can get the related dataset 
directly with this call:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-02-kafka-to-http/4-get-dataset.json -X POST "http://localhost:28181/management/v2/catalog/dataset/request" -s | jq
```

The output will be something like:
```json
{
  "@id": "kafka-stream-asset",
  "@type": "dcat:Dataset",
  "odrl:hasPolicy": {
    "@id": "Y29udHJhY3QtZGVmaW5pdGlvbg==:c3RyZWFtLWFzc2V0:NDlhYTUzZWEtMDUzMS00ZDkyLTg4Y2YtMGRjMTc4MmQ1NjY4",
    "@type": "odrl:Set",
    "odrl:permission": [],
    "odrl:prohibition": [],
    "odrl:obligation": [],
    "odrl:target": "kafka-stream-asset"
  },
  "dcat:distribution": {
    "@type": "dcat:Distribution",
    "dct:format": {
      "@id": "HttpData"
    },
    "dcat:accessService": "b24dfdbc-d17f-4d6e-9b5c-8fa71dacecfc"
  },
  "id": "kafka-stream-asset",
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
    "dct": "https://purl.org/dc/terms/",
    "dcat": "https://www.w3.org/ns/dcat/",
    "odrl": "http://www.w3.org/ns/odrl/2/",
    "dspace": "https://w3id.org/dspace/v0.8/"
  }
}
```

With the `odrl:hasPolicy/@id` we can now replace it in the [negotiate-contract.json](5-negotiate-contract.json) file
and request the contract negotiation:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-02-kafka-to-http/5-negotiate-contract.json  -X POST "http://localhost:28181/management/v2/contractnegotiations" -s | jq
```

### Start the transfer

First we need to set up the logging webserver on the consumer side that will receive a call for every new event. For this
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

If the `edc:contractAgreementId` is valued, it can be used to start the transfer, replacing it in the [6-transfer.json](6-transfer.json)
file to `{{contract-agreement-id}}` and then calling the connector with this command:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-02-kafka-to-http/6-transfer.json -X POST "http://localhost:28181/management/v2/transferprocesses" -s | jq
```
> Note that the destination address is `localhost:4000`, this because is where our logging webserver is listening.

Let's wait until the transfer state is `STARTED` state executing this call, replacing to `{{transfer-process-id}}` the id returned
by the start transfer call:
```shell
curl "http://localhost:28181/management/v2/transferprocesses/{{transfer-process-id}}" -s | jq
```

### Produce events

With the Kafka server running in Docker, you can use the Kafka command-line producer `kafka-console-producer.sh` to produce a message. In a new terminal shell, you'll need to execute:
```shell
docker exec -it kafka-kraft /opt/kafka/bin/kafka-console-producer.sh --topic kafka-stream-topic --bootstrap-server localhost:9092
```
This command will open an interactive prompt for you to input your message. Once you've typed your message and pressed Enter, it will be produced, consumed and pushed to the receiver server. You should observe the content being logged on its terminal shell:

```
Incoming request
Method: POST
Path: /
Body:
<message-sent>
```
