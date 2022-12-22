# Transfer samples

The samples in this scope revolve around the topic of transferring data between two connectors. Here you will learn
about the steps required for a transfer on provider as well as consumer side. The samples start with the simple example
of a local file transfer and then show different ways to tweak that transfer, before a transfer is performed between
different cloud providers.

> Before starting with these samples, be sure to check out the [basic samples](../basic/README.md)!

## Samples

### [Transfer sample 01](./transfer-01-file-transfer/README.md): Perform a local file transfer

In this sample you will perform your first data transfer. To keep it simple, a file is transferred on your local machine
from one directory to another. You will see which extensions and configurations are required for a transfer and learn
how to create a data offer as a provider as well as which steps to perform as a consumer.

### [Transfer sample 02](./transfer-02-file-transfer-listener/README.md): Implement a transfer listener

As you'll learn in the first transfer sample, the process of a data transfer is executed in a state machine and
runs asynchronously in the background after being initiated. This sample is an enhancement of the previous sample and
shows how a listener can be used to immediately react to state changes in this asynchronous process.

### [Transfer sample 03](./transfer-03-modify-transferprocess/README.md): Modify a TransferProcess

This sample is another enhancement of the first transfer sample. After you've learned how to react to state changes
during a data transfer, here you will see how to actively modify a process in the state machine in a thread-safe manner.

### [Transfer sample 04](./transfer-04-open-telemetry/README.md): Open Telemetry

Now that you've gotten familiar with the process of transferring data, this sample will show how `OpenTelemetry`,
`Jaeger`, `Prometheus` and `Micrometer` can be used to collect and visualize traces and metrics during this process.

### [Transfer sample 05](./transfer-05-file-transfer-cloud/README.md): Perform a file transfer between cloud providers

While performing a local file transfer is a simple and thereby good first transfer example, you will likely never
encounter this in a real-world scenario. So now we'll move on to a more complex transfer scenario, where a file is
transferred not in the local file system, but between two different cloud providers. In this sample you will set up
a provider that offers a file located in an `Azure Blob Storage`, and a consumer that requests to transfer this file
to an `AWS S3 bucket`. Terraform is used for creating all required cloud resources.
