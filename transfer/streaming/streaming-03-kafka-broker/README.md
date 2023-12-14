# Streaming KAFKA to KAFKA

This sample demonstrates how to set up the Eclipse Dataspace Connector to stream messages through Kafka.
This code is only for demonstration purposes and should not be used in production.

## Concept

In this sample the dataplane is not used, the consumer will set up a kafka client to poll the messages from the broker
using some credentials obtained from the transfer process.

The DataFlow is managed by the [KafkaToKafkaDataFlowController](streaming-03-runtime/src/main/java/org/eclipse/edc/samples/streaming/KafkaToKafkaDataFlowController.java),
that on flow initialization creates an `EndpointDataReference` containing the credentials that the consumer would then use
to poll the messages.

### Run

Build the connector runtime, which will be used both for the provider and consumer:
```shell
./gradlew :transfer:streaming:streaming-03-kafka-broker:streaming-03-runtime:build
```

Run the provider and the consumer with their own configuration, which will need to be started from different terminals:

```shell
export EDC_FS_CONFIG=transfer/streaming/streaming-03-kafka-broker/streaming-03-runtime/provider.properties
java -jar transfer/streaming/streaming-03-kafka-broker/streaming-03-runtime/build/libs/connector.jar
```

```shell
#consumer
export EDC_FS_CONFIG=transfer/streaming/streaming-03-kafka-broker/streaming-03-runtime/consumer.properties
java -jar transfer/streaming/streaming-03-kafka-broker/streaming-03-runtime/build/libs/connector.jar
```

### Start Kafka and configure ACLs

Kafka will be started in [KRaft mode](https://developer.confluent.io/learn/kraft/), a single broker with `SASL_PLAINTEXT`
as security protocol ([see config](kafka.env)), there will be an `admin` user, responsible for setting up ACLs and producing
messages, and `alice`, that will be used by the consumer to consume the messages.

Run the Kafka container:
```shell
docker run --rm --name=kafka-kraft -h kafka-kraft -p 9093:9093 \
    -v "$PWD/transfer/streaming/streaming-03-kafka-broker/kafka-config":/config \
    --env-file transfer/streaming/streaming-03-kafka-broker/kafka.env \
    -e KAFKA_NODE_ID=1 \
    -e KAFKA_LISTENERS='PLAINTEXT://0.0.0.0:9093,BROKER://0.0.0.0:9092,CONTROLLER://0.0.0.0:9094' \
    -e KAFKA_ADVERTISED_LISTENERS='PLAINTEXT://localhost:9093,BROKER://localhost:9092' \
    -e KAFKA_PROCESS_ROLES='broker,controller' \
    -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
    -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
    -e KAFKA_CONTROLLER_QUORUM_VOTERS='1@localhost:9094' \
    -e KAFKA_INTER_BROKER_LISTENER_NAME='BROKER' \
    -e KAFKA_CONTROLLER_LISTENER_NAMES='CONTROLLER' \
    -e KAFKA_OFFSETS_TOPIC_NUM_PARTITIONS=1 \
    -e CLUSTER_ID='4L6g3nShT-eMCtK--X86sw' \
    confluentinc/cp-kafka:7.5.2
```

Create the topic `kafka-stream-topic`
```shell
docker exec -it kafka-kraft /bin/kafka-topics \
  --topic kafka-stream-topic --create --partitions 1 --replication-factor 1 \
  --command-config=/config/admin.properties \
  --bootstrap-server localhost:9092
```

To give `alice` read permissions on the topic we need to set up ACLs:
```shell
docker exec -it kafka-kraft /bin/kafka-acls --command-config /config/admin.properties \
  --bootstrap-server localhost:9093 \
  --add --allow-principal 'User:alice' \
  --topic kafka-stream-topic \
  --group group_id \
  --operation Read
```

### Register Asset, Policy Definition and Contract Definition on provider

Then put values of `kafka.bootstrap.servers` and `topic` in the [1-asset.json](1-asset.json) file replacing
their placeholders this way:
```json
{
  "dataAddress": {
    "type": "Kafka",
    "kafka.bootstrap.servers": "localhost:9093",
    "topic": "kafka-stream-topic"
  }
}
```

Then use these three calls to create the Asset, the Policy Definition and the Contract Definition:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-03-kafka-broker/1-asset.json -X POST "http://localhost:18181/management/v3/assets" -s | jq
```

```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-03-kafka-broker/2-policy-definition.json  -X POST "http://localhost:18181/management/v2/policydefinitions" -s | jq
```

```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-03-kafka-broker/3-contract-definition.json  -X POST "http://localhost:18181/management/v2/contractdefinitions" -s | jq
```

### Negotiate the contract

The typical flow requires fetching the catalog from the consumer side and using the contract offer to negotiate a contract. 
However, in this sample case, we already have the provider asset (`"kafka-stream-asset"`) so we can get the related dataset 
directly with this call:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-03-kafka-broker/4-get-dataset.json -X POST "http://localhost:28181/management/v2/catalog/dataset/request" -s | jq
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
  "dcat:distribution": [],
  "edc:id": "kafka-stream-asset",
  "@context": {
    "dct": "https://purl.org/dc/terms/",
    "edc": "https://w3id.org/edc/v0.0.1/ns/",
    "dcat": "https://www.w3.org/ns/dcat/",
    "odrl": "http://www.w3.org/ns/odrl/2/",
    "dspace": "https://w3id.org/dspace/v0.8/"
  }
}
```

With the `odrl:hasPolicy/@id` we can now replace it in the [negotiate-contract.json](5-negotiate-contract.json) file
and negotiate the contract:
```shell
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-03-kafka-broker/5-negotiate-contract.json  -X POST "http://localhost:28181/management/v2/contractnegotiations" -s | jq
```

### Start the transfer

First we need to set up the logging webserver on the consumer side that will receive the EndpointDataReference containing
the address and credentials to connect to the broker and poll the messages from the topic. For this you'll need to open
another terminal shell and run:
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
curl -H 'Content-Type: application/json' -d @transfer/streaming/streaming-03-kafka-broker/6-transfer.json -X POST "http://localhost:28181/management/v2/transferprocesses" -s | jq
```
> Note that the destination address is `localhost:4000`, this because is where our logging webserver is listening.

Let's wait until the transfer state is `STARTED` state executing this call, replacing to `{{transfer-process-id}}` the id returned
by the start transfer call:
```shell
curl "http://localhost:28181/management/v2/transferprocesses/{{transfer-process-id}}" -s | jq
```

### Consume events
Now in the console of the `http-request-logger` we started before, the `EndpointDataReference` should have appeared:
```json
{
  "id":"8c52a781-2588-4c9b-8c70-4e5ad428eea9",
  "endpoint": "localhost:9093",
  "authKey": "alice",
  "authCode": "alice-secret",
  "properties": {
    "https://w3id.org/edc/v0.0.1/ns/topic": "kafka-stream-topic"
  }
}
```

Using these information on the consumer side we can run a `kafka-console-consumer` with the data received to consume
messages from the topic:
```shell
docker exec -it kafka-kraft /bin/kafka-console-consumer --topic kafka-stream-topic \
  --bootstrap-server localhost:9093 \
  --consumer-property group.id=group_id \
  --consumer-property security.protocol=SASL_PLAINTEXT \
  --consumer-property sasl.mechanism=PLAIN \
  --consumer-property sasl.jaas.config='org.apache.kafka.common.security.plain.PlainLoginModule required username="alice" password="alice-secret";'
```

### Produce events

In another shell we can put ourselves in the provider shoes and create messages from the producer shell:
```shell
docker exec -it kafka-kraft /bin/kafka-console-producer --topic kafka-stream-topic \
  --producer.config=/config/admin.properties \
  --bootstrap-server localhost:9093
```

For every message created on the provider side we will see a message on the consumer side.
