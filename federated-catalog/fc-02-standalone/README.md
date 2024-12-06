# Standalone Federated Catalog


In this sample we focus on the implementation of a standalone federated catalog.
Similar to the previous sample [fc-01-embedded](../fc-01-embedded), 
the [standalone-fc](./standalone-fc) also builds on the 
functionalities of the federated catalog. However, unlike the previous one, it does not 
include the additional features of a connector.

This sample will go through:

* Implementation of a standalone FC
* Set up of the standalone FC
* Test catalog API endpoint of the standalone FC


### 1. Implementation a standalone FC
The [build.gradle.kts](../fc-02-standalone/standalone-fc/build.gradle.kts) 
file located in the [fc-02-standalone/standalone-fc](./standalone-fc)
directory includes all the necessary dependencies for creating a standalone federated catalog. This includes the `fc-00-basic:federated-catalog-base`,
and `fc-00-basic:federated-catalog-base` to enable the Catalog Node Resolver.

```kotlin
dependencies {
    runtimeOnly(project(":federated-catalog:fc-00-basic:federated-catalog-base"))
    runtimeOnly(project(":federated-catalog:fc-00-basic:static-node-resolver"))
}
```

Since we are using `fc-00-basic:federated-catalog-base` as our Catalog Node Resolver here 
as well, it will return only 
a single target catalog node; the `participant-connector` that we had set up in [fc-00-basic](../fc-00-basic/README.md). 
Querying the catalog API will therefore yield just one catalog, which is the contract offered by this connector.

The [config.properties](./standalone-fc/config.properties) file contains the necessary configurations, 
like the `web.http.catalog.path`, which is the catalog API endpoint of this standalone FC.

```properties
web.http.catalog.path=/api/catalog
web.http.catalog.port=39195
```

### 2. Start the fc-connector
#### Build the fc-connector JAR
Execute this command in project root to build the `standalone-fc` JAR file:

```bash
./gradlew federated-catalog:fc-02-standalone:standalone-fc:build
```


#### Run the fc-connector

To run the federated catalog, execute the following command

```shell
java -Dedc.fs.config=federated-catalog/fc-02-standalone/standalone-fc/config.properties -jar federated-catalog/fc-02-standalone/standalone-fc/build/libs/standalone-fc.jar
```

If the execution is successful, then the Catalog API of our standalone FC will listen on port `39195`.



## Test catalog query API
Before requesting the catalog API, make sure the `partcipant-connector` that we have set up in the 
[fc-00-basic](../fc-00-basic) is running, and it has a contract offer.

To get the combined set of catalogs, use the following request:

```http request
curl -d @federated-catalog/fc-01-embedded/resources/empty-query.json \
  -H 'content-type: application/json' http://localhost:39195/api/catalog/v1alpha/catalog/query \
  -s | jq
```

Sample output:
```json
[
  {
    "@id": "a8a8cd64-269d-485c-8857-74d08b13ae3c",
    "@type": "dcat:Catalog",
    "dcat:dataset": {
      "@id": "assetId",
      "@type": "dcat:Dataset",
      "odrl:hasPolicy": {
        "@id": "MQ==:YXNzZXRJZA==:MjJmNDlhYTAtM2I3YS00ODkzLTkwZDctNTU5MTZhNmViOWJk"
        
      },
      "dcat:distribution": [
        
      ],
      "name": "product description",
      "id": "assetId",
      "contenttype": "application/json"
    },
    "dcat:distribution": [],
    "dcat:service": {
      
    },
    "dspace:participantId": "provider",
    "originator": "http://localhost:19194/protocol",
    "@context": {
      
    }
  }
]
```