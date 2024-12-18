# Target Node Resolver - Static Node Directory
The Federated Catalog requires a list of Target Catalog Nodes (TCN), which are essentially the participant connectors in the dataspace.
The catalog crawler then crawls the DSP endpoints of these listed nodes, and stores the consolidated set of catalogs in a Federated Catalog Cache (FCC).


This list of Target Catalog Nodes, represented by `TargetNodes`, 
is provided by the `TargetNodeDirectory`.
This `TargetNodeDirectory` serves as a 'phone book', maintaining specific information about the 
dataspace participants. It accepts an initial list of participants (e.g. list of participants' 
IDs), and resolves this input to a list of TargetNodes.

The initial participant list may vary in its source and format depending on specific use cases. 
To accommodate these variations, different implementations of the TargetNodeDirectory can be 
adapted to customize the resolution process of Target Nodes from the provided participant list. 
In this sample, we will build a Catalog Node Resolver that reads the participants' data from a 
static file, [participants.json](./target-node-resolver/src/main/resources/participants.json) 
and resolves it into TargetNodes.


The code in this sample has been organized into several Java modules:
- `target-node-resolver`: contains `CatalogNodeDirectory`, an implementation of 
`TargetNodeDirectory`, which accepts the [`participants.json`](./target-node-resolver/src/main/resources/participants.json) 
and returns a list of TargetNodes.
- `embedded|standalone-fc-with-node-resolver`: the embedded/ standalone federated catalog that will be using the `catalog-node-resolver`.


## Implement the Catalog Node Resolver

### Participant file
To keep things straightforward, in this sample we will store our participant list in a static
json file, [participant.json](./target-node-resolver/src/main/resources/participants.json), that contains the `TargetNode`
properties of the dataspace participants.
In this case, the file contains the properties of the `participant-connector` from [fc-00-basic](../fc-00-basic).

```json 
{
    "name": "https://w3id.org/edc/v0.0.1/ns/",
    "id": "provider",
    "url": "http://localhost:19194/protocol",
    "supportedProtocols": ["dataspace-protocol-http"]
}
```
However, this solution is intended for use only within the sample scope; in production, it must be managed in different way.

### Target Node Resolver

The [CatalogNodeDirectory](./target-node-resolver/src/main/java/org/eclipse/edc/sample/extension/fc/CatalogNodeDirectory.java) 
implements TargetNodeDirectory and overrides its `getAll()` method. 
In our implementation, this method maps the file content of [`participant.json`](./target-node-resolver/src/main/resources/participants.json) 
to a list of TargetNodes.

```java
public class CatalogNodeDirectory implements TargetNodeDirectory {
    //...
    @Override
    public List<TargetNode> getAll() {
        try {
            return objectMapper.readValue(participantFileContent, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //...
}
```
During the preparation phase of a crawler run, the FC ExecutionManager invokes this method 
to obtain the list of TargetNodes. 
The crawler requests the DSP endpoints of the participants and stores the
aggregated catalogs in a Federated Catalog Cache (FCC). 
In this example we are using the in-memory implementation of an FCC.

## Run Federated Catalog with Node Resolver

Previously, we discussed the implementation of standalone and embedded FCs. 
In this example, we introduce two separate modules,`standalone-fc-with-node-resolver` 
and `embedded-fc-with-node-resolver`, which demonstrate the implementation of each type 
of federated catalogs that uses the node resolver`.

Before requesting each of the federated catalog APIs, make sure the `partcipant-connector` that we have set up in the
[fc-00-basic](../fc-00-basic/README.md) is running, and it has a contract offer.

### Run standalone-fc with Node Resolver
Apply the following steps to run a standalone federated catalog that uses the implemented static `target-node-resolver`.

#### Build the standalone-fc JAR
Execute this command in project root:
```bash
./gradlew federated-catalog:fc-03-static-node-directory:standalone-fc-with-node-resolver:build
```

#### Run the standalone-fc

To run the federated catalog, execute the following command

```shell
java -Dedc.fs.config=federated-catalog/fc-02-standalone/standalone-fc/config.properties -jar federated-catalog/fc-03-static-node-directory/standalone-fc-with-node-resolver/build/libs/standalone-fc-with-node-resolver.jar
```

If the execution is successful, then the Catalog API of our standalone FC will listen on port `39195`.

#### Test catalog query API

To get the combined set of catalogs, use the following request:

```http request
curl -d @federated-catalog/fc-01-embedded/resources/empty-query.json \
  -H 'content-type: application/json' http://localhost:39195/api/catalog/v1alpha/catalog/query \
  -s | jq
```

### Run embedded-FC with Node Resolver
Apply the following steps to run an embedded federated catalog connector that uses the implemented static `target-node-resolver`.

#### Build the fc-connector JAR
Execute this command in project root:

```bash
./gradlew federated-catalog:fc-03-static-node-directory:embedded-fc-with-node-resolver:build
```

#### Run the fc-connector

To run the federated catalog, execute the following command

```shell
java -Dedc.fs.config=federated-catalog/fc-01-embedded/fc-connector/config.properties -jar federated-catalog/fc-03-static-node-directory/embedded-fc-with-node-resolver/build/libs/fc-connector-with-node-resolver.jar
```

If the execution is successful, then the Catalog API of our standalone FC will listen on port `29195`.

#### Test catalog query API

To get the combined set of catalogs, use the following request:

```http request
curl -d @federated-catalog/fc-01-embedded/resources/empty-query.json \
  -H 'content-type: application/json' http://localhost:29195/api/catalog/v1alpha/catalog/query \
  -s | jq
```