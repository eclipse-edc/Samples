# Modify a TransferProcess

In the last transfer samples (`transfer-01` and `transfer-02`) we saw how data can be transferred easily, what a
`TransferProcess` is and how to react to it easily through the listener. This sample will show how `TransferProcess`
objects can be modified externally in a thread-safe and consistent way.

## Problem statement

The `TransferProcessManager` (TPM), which is the central state machine handling transfer processes, follows this basic
operational pattern:

1. take transfer process (TP) out of `TransferProcessStore` (TPS)
2. take appropriate action, e.g. provision or deprovision resources
3. update state of TP
4. put back into TPS

All those steps happen in a non-atomic way, so when a TP currently processed by the TPM is modified on another thread,
there is a strong possibility that that change will get overwritten or worse, may cause the state machine to be in an
illegal state.

A common pattern would be some sort of watchdog, where TPs that have not advanced their state in a given amount of time
are automatically cancelled or errored out. The following code snippet shows a typical TPM state transition:

```java
// get out of store
 var tpList = store.nextForState(IN_PROGRESS.code(),batchSize);
// take appropriate action, e.g. check if complete
var statusChecker=...;
foreach(var tp:tpList){
    if(statusChecker.isComplete()){
        //update state
        tp.transitionComplete();
        // put back into TPS
        store.update(tp);
    }
}
```
and then consider a watchdog that runs on another thread and fires every X minutes
```java

private void watchDog(){
  var longRunningTpList = store.nextForState(IN_PROGRESS.code(), 5);
  // filter list based on last state update
  var longRunningTpList = /*filter expression*/;
  for(var tp : longRunningTpList){
      tp.transitionError("timeout");
      store.update(tp);
  }
}
```

Now the problem becomes apparent when the `watchDog()` fires exactly here:
```java
//...
    if(statusChecker.isComplete()){
        
        // |<-- watchDog() fires here!
            
        //update state
        tp.transitionComplete();
        // ...
    }
```

then the TP would first go to the `ERROR` state, but then immediately to the `COMPLETED` state, because the TPM and the watchdog
have different object references to the same TP. We essentially have a race condition at our hands, resulting in the TP never 
"erroring out".

## About this sample
Please note that this sample does _not actually transfer anything_, it merely shows how to modify a transfer process 
outside the main state machine.

Modules:
- `simulator`: used to insert a dummy transfer process, that never completes to simulate the use of a watchdog
- `watchdog`: spins up a periodic task that checks for timed-out TPs and sets them to `ERROR`
- `consumer`: the build configuration

In order to run the sample, enter the following commands in a shell:

```bash
./gradlew transfer:transfer-03-modify-transferprocess:modify-transferprocess-consumer:build
java -Dedc.fs.config=transfer/transfer-03-modify-transferprocess/modify-transferprocess-consumer/config.properties -jar transfer/transfer-03-modify-transferprocess/modify-transferprocess-consumer/build/libs/consumer.jar
```

---

[Previous Chapter](../transfer-02-file-transfer-listener/README.md) | [Next Chapter](../transfer-04-open-telemetry/README.md)
