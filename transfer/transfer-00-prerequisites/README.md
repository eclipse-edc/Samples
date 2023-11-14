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

The section bellow will show you some explanation about some of the properties that you can find in
the configuration files.

#### 1. edc.receiver.http.endpoint

This property is used to define the endpoint where the connector consumer will send the
EndpointDataReference.

#### 2. edc.dataplane.token.validation.endpoint

This property is used to define the endpoint exposed by the control plane to validate the token.

### 2. Run the connectors

To run the provider, just run the following command

```bash
java -Dedc.keystore=transfer/transfer-00-prerequisites/resources/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.vault=transfer/transfer-00-prerequisites/resources/configuration/provider-vault.properties -Dedc.fs.config=transfer/transfer-00-prerequisites/resources/configuration/provider-configuration.properties -jar transfer/transfer-00-prerequisites/connector/build/libs/connector.jar
```

To run the consumer, just run the following command (different terminal)

```bash
java -Dedc.keystore=transfer/transfer-00-prerequisites/resources/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.vault=transfer/transfer-00-prerequisites/resources/configuration/consumer-vault.properties -Dedc.fs.config=transfer/transfer-00-prerequisites/resources/configuration/consumer-configuration.properties -jar transfer/transfer-00-prerequisites/connector/build/libs/connector.jar
```

Assuming you didn't change the ports in config files, the consumer will listen on the
ports `29191`, `29192` (management API) and `29292` (DSP API) and the provider will listen on the
ports `12181`, `19182` (management API) and `19282` (DSP API).

Running this sample consists of multiple steps, that are executed one by one and following the same
order.

### 3. Register data plane instance for provider

Before a consumer can start talking to a provider, it is necessary to register the data plane
instance of a connector. This is done by sending a POST request to the management API of the
provider connector. The [request body](resources/dataplane/register-data-plane-provider.json) should contain the data plane instance of the consumer
connector.

The registration of the provider data plane instance is done by sending a POST
request to the management API of the connector.

Open a new terminal and execute:

```bash
curl -H 'Content-Type: application/json' \
     -d @transfer/transfer-00-prerequisites/resources/dataplane/register-data-plane-provider.json \
     -X POST "http://localhost:19193/management/v2/dataplanes" -s | jq
```

The connectors have been configured successfully and are ready to be used.

[Next Chapter](../transfer-01-negotiation/README.md)
