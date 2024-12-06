# Embedded Federated Catalog


This sample demonstrates how we can implement a federated catalog which is embedded in a connector.
We will build one connector of such type and will call it `fc-connector`.
As discussed in the prerequisite sample [fc-00-basic](../fc-00-basic/README.md), 
we will be using the Node Resolver 
[fixed-node-resolver](../fc-00-basic/fixed-node-resolver) as a dependency in our embedded federated catalog. 
Also, the `participant-connector` we set up in the previous sample should still be running. 
This `participant-connector` will act as a provider, while the new `fc-connector` will act as a consumer.



This sample will go through:

* Implementation of an embedded FC
* Set up of the embedded FC, `fc-connector`
* Test catalog API endpoint of the `fc-connector`


### 1. Implementation the fc-connector
The [build.gradle.kts](../fc-01-embedded/fc-connector/build.gradle.kts) 
file located in the [fc-01-embedded/fc-connector](../fc-01-embedded/fc-connector) 
directory includes all the necessary dependencies for creating a connector, along with the `fc-00-basic:federated-catalog-base` 
needed to trigger the FC. Additionally, we need to add `fc-00-basic:federated-catalog-base` as a dependency to enable the Catalog Node Resolver.

```kotlin
dependencies {
    runtimeOnly(project(":federated-catalog:fc-00-basic:federated-catalog-base"))
    runtimeOnly(project(":federated-catalog:fc-00-basic:static-node-resolver"))
}
```

The [config.properties](../fc-01-embedded/fc-connector/config.properties) 
file contains the necessary configurations 
for this `fc-connector`, including the standard settings for a regular connector, along with additional configurations for a 
federated catalog, such as catalog api endpoint and crawler execution interval.

```properties
web.http.catalog.path=/api/catalog
web.http.catalog.port=29195

edc.catalog.cache.execution.delay.seconds=5
edc.catalog.cache.execution.period.seconds=5
edc.catalog.cache.partition.num.crawlers=5
```

### 2. Start the fc-connector
#### Build the fc-connector JAR
Execute this command in project root to build the `fc-connector` JAR file:

```bash
./gradlew federated-catalog:fc-01-embedded:fc-connector:build
```


#### Run the fc-connector

To run the connector, execute the following command

```shell
java -Dedc.fs.config=federated-catalog/fc-01-embedded/fc-connector/config.properties -jar federated-catalog/fc-01-embedded/fc-connector/build/libs/fc-connector.jar
```

If the execution is successful, then the Catalog API of our `fc-connector` will listen on port `29195`.

If you observe the logs, you can see the following recurring lines,

> DEBUG 2024-11-14T13:53:48.472700883 [ExecutionManager] Run pre-execution task
>
>DEBUG 2024-11-14T13:53:48.494149928 [ExecutionManager] Loaded 1 work items from storage
>
>DEBUG 2024-11-14T13:53:48.495574504 [ExecutionManager] Crawler parallelism is 1, based on config and number of work items
>
>DEBUG 2024-11-14T13:53:48.497891576 [ExecutionManager] Crawler-f81f5514-5c7f-44aa-94bb-16998861789b: WorkItem acquired
>
>DEBUG 2024-11-14T13:53:48.790873233 [ExecutionManager] Crawler [Crawler-f81f5514-5c7f-44aa-94bb-16998861789b] is done


This means our FC crawler is running, and the crawler found one node, which is the `participant-connector` we had set up before.



### 3. Test catalog query API

To query the catalogs from `fc-connector` side, we can now call the catalog API of our embedded federated catalog. 
Use the following request to invoke the catalog API:

```http request
curl -d @federated-catalog/fc-01-embedded/resources/empty-query.json \
  -H 'content-type: application/json' http://localhost:29195/api/catalog/v1alpha/catalog/query \
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