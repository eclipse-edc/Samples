# Implement a simple event consumer

In this sample, we build upon the [Provider push](../transfer-02-provider-push/README.md) sample to add functionality
to react to transfer startup on the consumer connector side.

The relevant code in this example has been put into this module
- `consumer-with-subscriber`: the consumer connector which an extension that subscribe to events.

## Subscribe to events

The `EventRouter` service is the gateway for all the EDC domain events, so we can register an `EventSubscriber` on a 
particular event implementation:
```java
eventRouter.register(TransferProcessStarted.class, new TransferProcessStartedSubscriber(monitor));
```

By doing this, every time a `TransferProcessStarted` gets published, the `TransferProcessStartedSubscriber` gets invoked.

> NOTE: `register` register an async subscriber. To register a sync subscriber that execs logic in the same transaction
> boundary of the code that emitted the event, `registerSync` can be used instead.

> NOTE: the hierarchy of the `Event` class can be used to subscribe to multiple events, e.g. by using `Event`, the subscriber
> will receive all the events.

The `TransferProcessStartedSubscriber` implements the `EventSubscriber` interface. 
The method `on` will be triggered when a `TransferProcessStarted` event gets published.

```java
class TransferProcessStartedSubscriber implements EventSubscriber {
    private final Monitor monitor;

    public TransferProcessStartedSubscriber(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public <E extends Event> void on(EventEnvelope<E> event) {
        monitor.info("TransferProcessStarted event has been emitted.");
        // do something after transfer has been started
    }
}
```

## Run the sample

Assuming your provider connector and logging webserver are still running, we can re-use the existing assets and contract definitions stored on 
provider side. If not, set up your assets and contract definitions as described in the [Negotiation](../transfer-01-negotiation/README.md) 
chapter.

### 1. Build & launch the consumer with listener extension

This consumer connector is based on a different build file, hence a new JAR file will be built. 
Make sure to terminate your current consumer connector from the previous chapters. 
That way we unblock the ports and can reuse the known configuration files and API calls.

Run this to build and launch the consumer with listener extension:

```bash
./gradlew transfer:transfer-04-event-consumer:consumer-with-listener:build
java -Dedc.fs.config=transfer/transfer-00-prerequisites/resources/configuration/consumer-configuration.properties -jar \
  transfer/transfer-04-event-consumer/consumer-with-subscriber/build/libs/connector.jar
````

### 2. Negotiate a new contract

```bash
curl -d @transfer/transfer-01-negotiation/resources/negotiate-contract.json \
  -X POST -H 'content-type: application/json' http://localhost:29193/management/v3/contractnegotiations \
  -s | jq
```

### 3. Get the contract agreement id

```bash
curl -X GET "http://localhost:29193/management/v3/contractnegotiations/{{contract-negotiation-id}}" \
    --header 'Content-Type: application/json' \
    -s | jq
```

### 4. Perform a file transfer

#### Start a http server

As a pre-requisite, you need to have a logging webserver that runs on port 4000 and logs all the incoming requests, the
data will be sent to this server.

```bash
docker build -t http-request-logger util/http-request-logger
docker run -p 4000:4000 http-request-logger
```

Replace the `contractId` property inside the [request body](../transfer-02-provider-push/resources/start-transfer.json)
with the contract agreement id from the previous call.
Afterward run:

```bash
curl -X POST "http://localhost:29193/management/v3/transferprocesses" \
  -H "Content-Type: application/json" \
  -d @transfer/transfer-02-provider-push/resources/start-transfer.json \
  -s | jq
```

### 5. Inspect the logs

The consumer should spew out logs similar to:

```bash
INFO 2023-10-16T09:29:46.271592 TransferProcessStarted event has been emitted.   <----------------------------
```

If you see the `TransferProcessStarted event has been emitted.` log message, it means that your event consumer has been
configured successfully.

[Next Chapter](../transfer-05-file-transfer-cloud/README.md)
