# Implement a simple event consumer

In this sample, we build upon the [Provider push](../transfer-02-provider-push/README.md) chapter to add functionality
to react to transfer completion on the consumer connector side.

Also, in order to keep things organized, the code in this example has been separated into several Java modules:

- `consumer-with-listener`: the consumer connector which will be extended by the event consumer
- `listener`: contains the `TransferProcessListener` implementation which will consume an event

## Inspect the listener

A `TransferProcessListener` may define methods that are invoked after a transfer changes state, for example, to notify an
external application on the consumer side after data has been produced (i.e. the transfer moves to the completed state).

```java
// in TransferListenerExtension.java
    @Override
    public void initialize(ServiceExtensionContext context) {
        // ...
        var transferProcessObservable = context.getService(TransferProcessObservable.class);
        transferProcessObservable.registerListener(new MarkerFileCreator(monitor));
    }
```

The `TransferProcessStartedListener` implements the `TransferProcessListener` interface. 
It will consume the transfer `STARTED` event and write a log message.

```java
public class TransferProcessStartedListener implements TransferProcessListener {

    private final Monitor monitor;

    public TransferProcessStartedListener(Monitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Callback invoked by the EDC framework when a transfer is about to be completed.
     *
     * @param process the transfer process that is about to be completed.
     */
    @Override
    public void preStarted(final TransferProcess process) {
        monitor.debug("TransferProcessStartedListener received STARTED event");
        // do something meaningful before transfer start
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
java -Dedc.fs.config=transfer/transfer-00-prerequisites/resources/configuration/consumer-configuration.properties -jar transfer/transfer-04-event-consumer/consumer-with-listener/build/libs/connector.jar
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
INFO 2023-10-16T09:29:46.271592 TransferProcessStartedListener received STARTED event   <----------------------------
```

If you see the `TransferProcessStartedListener received STARTED event` log message, it means that your event consumer has been
configured successfully.

[Next Chapter](../transfer-05-file-transfer-cloud/README.md)
