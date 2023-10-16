# Implement a simple event consumer

In this sample, we build upon the [Consumer Pull](../transfer-02-consumer-pull/README.md) chapter to add functionality
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

Assuming your provider connector is still running, we can re-use the existing assets and contract definitions stored on 
provider side. If not, set up your assets and contract definitions as described in the [Negotiation](../transfer-01-negotiation/README.md) 
chapter.

### 1. Build & launch the consumer with listener extension

This consumer connector is based on a different build file, hence a new jar file will be produced. 
Make sure to terminate your current consumer connector from the previous chapters. 
That way we unblock the ports and can reuse the known configuration files and API calls.

Run this to build and launch the consumer with listener extension:

```bash
./gradlew transfer:transfer-04-event-consumer:consumer-with-listener:build
java -Dedc.keystore=transfer/transfer-00-prerequisites/resources/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.vault=transfer/transfer-00-prerequisites/resources/configuration/consumer-vault.properties -Dedc.fs.config=transfer/transfer-00-prerequisites/resources/configuration/consumer-configuration.properties -jar transfer/transfer-04-event-consumer/consumer-with-listener/build/libs/connector.jar
````

### 2. Register data plane instance for consumer

The data plane has to be registered again for the consumer connector.

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

### 3. Negotiate a new contract

```bash
curl -d '{
  "@context": {
    "edc": "https://w3id.org/edc/v0.0.1/ns/",
    "odrl": "http://www.w3.org/ns/odrl/2/"
  },
  "@type": "NegotiationInitiateRequestDto",
  "connectorId": "provider",
  "connectorAddress": "http://localhost:19194/protocol",
  "consumerId": "consumer",
  "providerId": "provider",
  "protocol": "dataspace-protocol-http",
  "offer": {
   "offerId": "MQ==:YXNzZXRJZA==:YTc4OGEwYjMtODRlZi00NWYwLTgwOWQtMGZjZTMwMGM3Y2Ey",
   "assetId": "assetId",
   "policy": {
     "@id": "MQ==:YXNzZXRJZA==:YTc4OGEwYjMtODRlZi00NWYwLTgwOWQtMGZjZTMwMGM3Y2Ey",
     "@type": "Set",
     "odrl:permission": [],
     "odrl:prohibition": [],
     "odrl:obligation": [],
     "odrl:target": "assetId"
   }
  }
}' -X POST -H 'content-type: application/json' http://localhost:29193/management/v2/contractnegotiations \
-s | jq
```

### 4. Get the contract agreement id

```bash
curl -X GET "http://localhost:29193/management/v2/contractnegotiations/<contract negotiation id, returned by the negotiation call>" \
    --header 'Content-Type: application/json' \
    -s | jq
```

### 5. Perform a file transfer

Replace the `contractId` property with the contract agreement id from the previous call.
Afterward run:

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
          "type": "HttpProxy"
        }
    }' \
    -s | jq
```

### 6. Inspect the logs

The consumer should spew out logs similar to:

```bash
DEBUG 2023-10-16T09:29:45.316908 [TransferProcessManagerImpl] TransferProcess 762b5a0c-43fb-4b8b-8022-669043c8fa81 is now in state REQUESTED
DEBUG 2023-10-16T09:29:46.269998 DSP: Incoming TransferStartMessage for class org.eclipse.edc.connector.transfer.spi.types.TransferProcess process: 762b5a0c-43fb-4b8b-8022-669043c8fa81
DEBUG 2023-10-16T09:29:46.271592 TransferProcessStartedListener received STARTED event   <----------------------------
DEBUG 2023-10-16T09:29:46.27174 TransferProcess 762b5a0c-43fb-4b8b-8022-669043c8fa81 is now in state STARTED
```

If you see the `TransferProcessStartedListener received STARTED event` log message, it means that your event consumer has been
configured successfully.

[Next Chapter](../transfer-05-open-telemetry/README.md)
