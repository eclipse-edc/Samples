# Transfer samples

The samples in this scope revolve around the topic of transferring data between two connectors. Here
you will learn about the steps required for a transfer on provider as well as consumer side. The
samples start with the simple example of a building and configuring the connector library.
Furthermore, you will learn about the contract negotiation workflow and how to perform a data transfer.

> Before starting with these samples, be sure to check out the [basic samples](../basic/README.md)!

## Samples

### [Transfer sample 00](./transfer-00-prerequisites/README.md): Prerequisites

This is a prerequisite for the following chapters. 
You will build the connector library here, configure and execute it.

### [Transfer sample 01](./transfer-01-negotiation/README.md): Negotiation

Before two connectors can exchange actual data, negotiation has to take place.
The final goal of this example is to showcase the negotiation workflow between two connectors so that
the actual data transfer can take place. This chapter is a prerequisite to the following chapters.

### [Transfer sample 02](transfer-02-provider-push/README.md): Perform a provider push exchange between a consumer and a provider

This sample demonstrates the "provider push" use case. 
More details on [Consumer Pull and Provider Push Transfers](https://eclipse-edc.github.io/documentation/for-adopters/control-plane/#consumer-pull-and-provider-push-transfers).

### [Transfer sample 03](./transfer-03-consumer-pull): Implement a custom Http Proxy for PULL transfer

This sample demonstrates how a custom HTTP Proxy Data Plane can be implemented to address PULL transfer type.
More details on [Consumer Pull and Provider Push Transfers](https://eclipse-edc.github.io/documentation/for-adopters/control-plane/#consumer-pull-and-provider-push-transfers).

### [Transfer sample 04](transfer-04-event-consumer/README.md): Consuming connector events

In this sample you will learn how to react to certain connector events.