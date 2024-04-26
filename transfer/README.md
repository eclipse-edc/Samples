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

### [Transfer sample 02](./transfer-02-consumer-pull/README.md): Perform a consumer pull exchange between a consumer and a provider

In this sample you will perform your first actual data transfer.
The purpose of this sample is to show a data exchange between 2 connectors, one representing the
data provider and the other, the consumer. It's based on a "consumer pull" use case that you can find
more details on [Transfer data plane documentation](https://github.com/eclipse-edc/Connector/tree/main/extensions/control-plane/transfer/transfer-data-plane).

### [Transfer sample 03](transfer-03-provider-push/README.md): Perform a provider push exchange between a consumer and a provider

This sample demonstrates the "provider push" use case that you can find more details
on [Transfer data plane documentation](https://github.com/eclipse-edc/Connector/tree/main/extensions/control-plane/transfer/transfer-data-plane).

### [Transfer sample 04](transfer-04-event-consumer/README.md): Consuming connector events

In this sample you will learn how to react to certain connector events.

### [Transfer sample 05](./transfer-05-file-transfer-cloud/README.md): Perform a file transfer between cloud providers

While performing a local file transfer is a simple and thereby good first transfer example, you will
likely never encounter this in a real-world scenario. So now we'll move on to a more complex
transfer scenario, where a file is transferred not in the local file system, but between two
different cloud providers. In this sample you will set up
a provider that offers a file located in an `Azure Blob Storage`, and a consumer that requests to
transfer this file to an `AWS S3 bucket`. Terraform is used for creating all required cloud
resources.