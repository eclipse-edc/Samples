# Subscribe to EDC events

This sample shows how to subscribe to EDC domain events using the `EventRouter`.
A single control-plane connector is enough — no transfer setup or data plane required.

The relevant code is in the `connector-with-event-subscriber` module.

## Subscribe to events

The `EventRouter` service is the gateway for all EDC domain events.
Inject it into your `ServiceExtension` and call `register` with the event type you care about:

```java
@Inject
private EventRouter eventRouter;

@Override
public void initialize(ServiceExtensionContext context) {
    eventRouter.register(AssetCreated.class, new AssetCreatedSubscriber(context.getMonitor()));
}
```

The subscriber implements `EventSubscriber`. Its `on` method is called whenever the registered event is published:

```java
@Override
public <E extends Event> void on(EventEnvelope<E> envelope) {
    if (envelope.getPayload() instanceof AssetCreated event) {
        monitor.info("Asset created: " + event.getAssetId());
    }
}
```

> **Note:** `register` registers an async subscriber that runs outside the originating transaction.
> Use `registerSync` instead to run inside the same transaction boundary.

> **Note:** You can subscribe to a parent type to receive all events in a hierarchy.
> Registering against `AssetEvent.class` catches `AssetCreated`, `AssetUpdated`, and `AssetDeleted`.
> Registering against `Event.class` catches every event in the system.

## Run the sample

### 1. Build & launch the connector

```bash
./gradlew basic:basic-04-event-consumer:connector-with-event-subscriber:build
java -Dedc.fs.config=transfer/transfer-00-prerequisites/resources/configuration/consumer-configuration.properties \
  -jar basic/basic-04-event-consumer/connector-with-event-subscriber/build/libs/connector.jar
```

### 2. Create an asset

```bash
curl -d @basic/basic-04-event-consumer/resources/create-asset.json \
  -X POST -H 'content-type: application/json' http://localhost:29193/management/v3/assets \
  -s | jq
```

### 3. Inspect the logs

The connector should log:

```
INFO 2024-01-01T00:00:00.000 Asset created: assetId
```

[Previous Chapter](../basic-03-configuration/README.md)
