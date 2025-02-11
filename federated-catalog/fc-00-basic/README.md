# Federated Catalog Prerequisites

The purpose of this example is to make preparations for implementing Federated Catalog (FC) 
and set up additional requirements for testing the FC functionalities.
For this purpose, we will be covering the following in this scope.
* `federated-catalog-base`: A basic federated catalog that includes necessary dependencies for triggering the FC.
* `participant-connector`: A connector with a contract offer. We will require this for testing the functionalities of the FCs in our later samples.
* `fixed-node-resolver`: A mock node directory resolver. 
It provides a fixed Target Catalog Node, which represents the `participant-connector`.



### federated-catalog-base
The [federated-catalog-base](../fc-00-basic/federated-catalog-base) will be used as a foundational module in our upcoming samples to trigger the FC.
It provides a [build.gradle.kts](./federated-catalog-base/build.gradle.kts) file that includes only the dependencies
essential for FC, without any additional functionality.
```shell
...
dependencies {
    implementation(libs.edc.fc.spi.crawler)
    runtimeOnly(libs.fc.core)
    runtimeOnly(libs.fc.ext.api)
}
...
```
Any further dependencies will be added in the later samples based on their use cases.


### fixed-node-resolver
The Federated Catalog requires a list of Target Catalog Nodes, which are essentially the DSP endpoints of the dataspace participants.
The catalog crawler then crawls these listed endpoints to collect their offered catalogs. 
This list of Target Nodes is resolved by a Catalog Node Resolver which implements the `TargetNodeDirectory`.
Check out [eclipse-edc/FederatedCatalog](https://github.com/eclipse-edc/FederatedCatalog) for further information on this topic.


In this module, we've included a fixed Node Resolver, [fixed-node-resolver](./fixed-node-resolver)
that simply returns a hard-coded Target Node of the `participant-connector`.
However, we will not cover the implementation of the resolver in this sample; that will be explained in detail in later samples.


The purpose of including this [`fixed-node-resolver`](./fixed-node-resolver)
as a prerequisite, is the fact that we need to have some form of Target Node Resolver to demonstrate the functionality
of the federated catalogs that we are going to build in sample 
[fc-01-embedded](../fc-01-embedded) and [fc-02-standalone](../fc-02-standalone).

### participant connector

When the federated catalog boots up, the crawler begins periodically invoking the Target Nodes returned by the 
Node Resolver and collecting the catalogs offered by these nodes. To test whether our federated catalogs 
(which we will build in later samples: [fc-01-embedded](../fc-01-embedded) and [fc-02-standalone](../fc-02-standalone)) 
can successfully request and retrieve these catalogs, we need at least one connector with a contract offer.

Therefore, in this section, we will start a connector and then create a contract 
for this connector. In the future samples, we will refer to it as `participant-connector`.
This `participant-connector` will function as a provider.
We will use the resources from the [transfer](../../transfer) sample to set up this connector. In the rest of this section we will,
* run the `participant-connector`
* create an asset for this `participant-connector`
* create a policy
* create a contract offer

Although these topics were covered in the [transfer](../../transfer) section, we’ll document all the necessary commands here for easier execution.


#### Build connector jar
Use the following command to build a connector jar.
```shell
./gradlew transfer:transfer-00-prerequisites:connector:build
```
#### Run the connector
Execute the following to run the connector jar.
```shell
java -Dedc.fs.config=transfer/transfer-00-prerequisites/resources/configuration/provider-configuration.properties -jar transfer/transfer-00-prerequisites/connector/build/libs/connector.jar
```

---

Once the connector is running, carry out the commands below in sequence.
#### Create an asset
```shell
curl -d @transfer/transfer-01-negotiation/resources/create-asset.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets \
  -s | jq
```

#### Create a policy
```bash
curl -d @transfer/transfer-01-negotiation/resources/create-policy.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/policydefinitions \
  -s | jq
```

#### Create a contract definition
```bash
curl -d @transfer/transfer-01-negotiation/resources/create-contract-definition.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/contractdefinitions \
  -s | jq
```
