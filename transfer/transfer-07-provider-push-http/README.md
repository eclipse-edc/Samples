# Implement a simple "Consumer Pull" Http transfer flow

The purpose of this example is to show a data exchange between 2 connectors, one representing the
data provider and the other, the consumer. It's based on a consumer pull usecase that you can find
more details
on [Transfer data plane documentation](https://github.com/eclipse-edc/Connector/tree/main/extensions/control-plane/transfer/transfer-data-plane)
For the sake of simplicity, the provider and the consumer
will be on the same machine, but in a real world configuration, they will likely be on different
machines. The final goal of this example is to present the steps through which the 2 connectors will
have to pass so that the consumer can have access to the data, held by the provider.

Those steps are the following:

* Running the provider connector
* Running the consumer connector
* Running a Http server that will receive the Endpoint Data Reference on the consumer side, that
  contains the url to be used to get the data.
* Register data plane instance for provider connector
* Register data plane instance for consumer connector
* Create an Asset on the provider (The asset will be the data to be shared)
* Create an access policy on the provider (The policy will define the access right to the data)
* Create a contract definition on the provider

At this step, the consumer should be able to fetch the catalog from the provider and to see the
contract offer generated from the resources that have been created.

Once the catalog is available, to access the data, the consumer should follow the following steps:

* Performing a contract negotiation with the provider
* Performing a transfer
    * The consumer will initiate a file transfer
    * The provider will send an EndpointDataReference to the consumer
* The consumer could reach the endpoint and access the data

Also, in order to keep things organized, the code in this example has been separated into several
Java modules:

* `connector`: contains the configuration and build files for both the
  consumer and the provider connector
* `backend-service`: represent the backend service where the consumer connector will send the
  EndpointDataReference to access the data

> For the sake of simplicity, we will use an in-memory catalog and fill it with just one single
> asset. This will be deleted after the provider shutdown.

### Provider connector

The provider connector is the one providing EndpointDataReference to the consumer after it initiates
a transfer.

### Consumer connector

The consumer is the one "requesting" the data to the provider.

# How to build a connector

In fact, in the configuration of our example, both the provider and the consumer are connectors.
Therefore, to set up our example, we need to start a connector with the configuration for a provider
and another one with the configuration of a consumer.

This section allows you to build the connector before launching it.

```bash
./gradlew transfer:transfer-07-provider-push-http:http-push-connector:build
```

After the build end you should verify that the connector jar is created in the directory
[http-push-connector.jar](http-push-connector/build/libs/http-push-connector.jar)

# How to run a connector

It is important to note that only the property file differs between the consumer and the provider.
You can find the configuration file in the directories below:

* [provider](http-push-provider/provider-configuration.properties)
* [consumer](http-push-consumer/consumer-configuration.properties)

The section bellow will show you some explanation about some of the properties that you can find in
the configuration files.

#### 1. edc.receiver.http.endpoint

This property is used to define the endpoint where the connector consumer will send the
EndpointDataReference.

#### 2. edc.dataplane.token.validation.endpoint

This property is used to define the endpoint exposed by the control plane to validate the token.

### 1. Run a provider

To run a provider, you should run the following command

```bash
java -Dedc.keystore=transfer/transfer-07-provider-push-http/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.vault=transfer/transfer-07-provider-push-http/http-push-provider/provider-vault.properties -Dedc.fs.config=transfer/transfer-07-provider-push-http/http-push-provider/provider-configuration.properties -jar transfer/transfer-07-provider-push-http/http-push-connector/build/libs/http-push-connector.jar
```

### 2. Run a consumer

To run a consumer, you should run the following command

```bash
java -Dedc.keystore=transfer/transfer-07-provider-push-http/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.vault=transfer/transfer-07-provider-push-http/http-push-consumer/consumer-vault.properties -Dedc.fs.config=transfer/transfer-07-provider-push-http/http-push-consumer/consumer-configuration.properties -jar transfer/transfer-07-provider-push-http/http-push-connector/build/libs/http-push-connector.jar
```

Assuming you didn't change the ports in config files, the consumer will listen on the
ports `29191`, `29192` (management API) and `29292` (IDS API) and the provider will listen on the
ports `12181`, `19182` (management API) and `19282` (IDS API).

# Run the sample

Running this sample consists of multiple steps, that are executed one by one and following the same
order.

> Please in case you have some issues with the jq option, not that it's not mandatory, and you can
> drop it from the command. it's just used to format the output, and the same advice should be
> applied to all calls that use `jq`.

### 1. Register data plane instance for provider

Before a consumer can start talking to a provider, it is necessary to register the data plane
instance of a connector. This is done by sending a POST request to the management API of the
provider connector. The request body should contain the data plane instance of the consumer
connector.

The registration of the provider data plane instance is done by sending a POST
request to the management API of the connector.

```bash
curl -H 'Content-Type: application/json' \
     -d '{
   "edctype": "dataspaceconnector:dataplaneinstance",
   "id": "http-push-provider-dataplane",
   "url": "http://localhost:19292/control/transfer",
   "allowedSourceTypes": [ "HttpData" ],
   "allowedDestTypes": [ "HttpProxy", "HttpData" ],
   "properties": {
     "publicApiUrl": "http://localhost:19291/public/"
   }
 }' \
     -X POST "http://localhost:19193/api/v1/data/instances"
```

### 2. Register data plane instance for consumer

The same thing that is done for the provider must be done for the consumer

```bash
curl -H 'Content-Type: application/json' \
     -d '{
   "edctype": "dataspaceconnector:dataplaneinstance",
   "id": "http-push-consumer-dataplane",
   "url": "http://localhost:29292/control/transfer",
   "allowedSourceTypes": [ "HttpData" ],
   "allowedDestTypes": [ "HttpProxy", "HttpData" ],
   "properties": {
     "publicApiUrl": "http://localhost:29291/public/"
   }
 }' \
     -X POST "http://localhost:29193/api/v1/data/instances"
```

### 3. Create an Asset on the provider side

The provider connector needs to transfer a file to the location specified by the consumer connector
when the data are requested. In order to offer any data, the provider must maintain an internal list
of resources offered, through a contract offer, the so-called "catalog".

The following request creates an asset on the provider connector.

```bash
curl -d '{
           "asset": {
             "properties": {
               "asset:prop:id": "assetId",
               "asset:prop:name": "product description",
               "asset:prop:contenttype": "application/json"
             }
           },
           "dataAddress": {
             "properties": {
               "name": "Test asset",
               "baseUrl": "https://jsonplaceholder.typicode.com/users",
               "type": "HttpData"
             }
           }
         }' -H 'content-type: application/json' http://localhost:19193/api/v1/data/assets \
         -s | jq
```

> It is important to note that the `baseUrl` property of the `dataAddress` is a fake data used for
> the purpose of this example. It will be the data that the consumer will pull on the sample
> execution.

### 4. Create a Policy on the provider

In order to manage the accessibility rules of an asset, it is essential to create a policy. However,
to keep things simple, we will choose a policy that gives direct access to all the assets that are
associated within the contract definitions.
This means that the consumer connector can request any asset from the provider connector.

```bash
curl -d '{
           "id": "aPolicy",
           "policy": {
             "uid": "231802-bb34-11ec-8422-0242ac120002",
             "permissions": [
               {
                 "target": "assetId",
                 "action": {
                   "type": "USE"
                 },
                 "edctype": "dataspaceconnector:permission"
               }
             ],
             "@type": {
               "@policytype": "set"
             }
           }
         }' -H 'content-type: application/json' http://localhost:19193/api/v1/data/policydefinitions \
         -s | jq
```

### 5. Create a contract definition on Provider

To ensure an exchange between providers and consumers, the supplier must create a contract offer for
the good, on the basis of which a contract agreement can be negotiated. The contract definition
associates policies to a selection of assets to generate the contract offers that will be put in the
catalog. In this case, the selection is empty, so every asset is attached to these policies

```bash
curl -d '{
           "id": "1",
           "accessPolicyId": "aPolicy",
           "contractPolicyId": "aPolicy",
           "criteria": []
         }' -H 'content-type: application/json' http://localhost:19193/api/v1/data/contractdefinitions \
         -s | jq
```

Sample output:

```json
{
  "createdAt": 1674578184023,
  "id": "1"
}
```

### 6. How to fetch catalog on consumer side

In order to offer any data, the consumer can fetch the catalog from the provider, that will contain
all the contract offers available for negotiation. In our case, it will contain a single contract
offer, the so-called "catalog". To get the catalog from the consumer side, you can use the following
endpoint:

```bash
curl -X POST "http://localhost:29193/api/v1/management/catalog/request" \
--header 'Content-Type: application/json' \
--data-raw '{
  "providerUrl": "http://localhost:19193/api/v1/ids/data"
}'
```