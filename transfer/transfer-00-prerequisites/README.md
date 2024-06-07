# Prerequisites

The purpose of this example is to make preparations for a connector-to-connector communication.
For that we'll set up two connectors.

One connector will be the "consumer" connector while the other will act as the
"provider" connector. More on that in the following chapters.

For the sake of simplicity, the provider and the consumer
will run on the same machine, but in a real world configuration, they will likely be on different
machines.

This sample will go through:

* Building the connector module
* Running the provider connector
* Running the consumer connector
* Registering data plane instance for the provider connector

## Run the sample

### 1. Build the connector

When we talk about a connector in the context of Eclipse Dataspace Components, we really mean a JAR file that runs on a machine.
Before we can run a connector, we need to build the JAR file.

Execute this command in project root:

```bash
./gradlew transfer:transfer-00-prerequisites:connector:build
```

After the build end you should verify that the connector.jar is created in the directory
[/connector/build/libs/connector.jar](connector/build/libs/connector.jar)

We can use the same .jar file for both connectors. Note that the consumer and provider connectors differ in their configuration.

Inspect the different configuration files below:

* [provider-configuration.properties](resources/configuration/provider-configuration.properties)
* [consumer-configuration.properties](resources/configuration/consumer-configuration.properties)

### 2. Run the connectors

To run the provider, just run the following command

```bash
java -Dedc.keystore=transfer/transfer-00-prerequisites/resources/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.fs.config=transfer/transfer-00-prerequisites/resources/configuration/provider-configuration.properties -jar transfer/transfer-00-prerequisites/connector/build/libs/connector.jar
```

To run the consumer, just run the following command (different terminal)

```bash
java -Dedc.keystore=transfer/transfer-00-prerequisites/resources/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.fs.config=transfer/transfer-00-prerequisites/resources/configuration/consumer-configuration.properties -jar transfer/transfer-00-prerequisites/connector/build/libs/connector.jar
```

Assuming you didn't change the ports in config files, the consumer will listen on the
ports `29191`, `29192` (management API) and `29292` (DSP API) and the provider will listen on the
ports `12181`, `19182` (management API) and `19282` (DSP API).

The connectors have been configured successfully and are ready to be used.

[Next Chapter](../transfer-01-negotiation/README.md)
