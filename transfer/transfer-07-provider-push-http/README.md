# Implement a simple "Provider Push" Http transfer flow

The purpose of this example is to show a data exchange between 2 connectors, one representing the
data provider and the other, the consumer. It's based on a provider push usecase that you can find
more details
on [Transfer data plane documentation](https://github.com/eclipse-edc/Connector/tree/main/extensions/control-plane/transfer/transfer-data-plane)
For the sake of simplicity, the provider and the consumer
will be on the same machine, but in a real world configuration, they will likely be on different
machines. The final goal of this example is to present the steps through which the 2 connectors will
have to pass so that the consumer can have access to the data, held by the provider.

Those steps are the following:

* Running the provider connector
* Running the consumer connector
* Register data plane instance for provider connector
* Create an Asset on the provider (The asset will be the data to be shared)
* Create an access policy on the provider (The policy will define the access right to the data)
* Create a contract definition on the provider

At this step, the consumer should be able to fetch the catalog from the provider and to see the
contract offer generated from the resources that have been created.

Once the catalog is available, to access the data, the sample will go through:

* Performing a contract negotiation with the provider
* Performing a transfer
    * The consumer will initiate a file transfer
    * Provider Control Plane retrieves the DataAddress of the actual data source and creates a
      DataFlowRequest based on the received DataRequest and this data address
* Provider Data Plane fetches data from the actual data source
* Provider Data Plane pushes data to the consumer service

Also, in order to keep things organized, the code in this example has been separated into one
Java module:

* `connector`: contains the configuration and build files for both the
  consumer and the provider connector
* `provider-push-backend-service`: represent the backend service where the provider will push the data after the
  consumer has initiated the transfer

> For the sake of simplicity, we will use an in-memory catalog and fill it with just one single
> asset. This will be deleted after the provider shutdown.

# How to build a connector

In fact, in the configuration of our example, both the provider and the consumer are connectors.
Therefore, to set up our example, we need to start a connector with the configuration for a provider
and another one with the configuration of a consumer.

This section allows you to build the connector before launching it.

```bash
./gradlew transfer:transfer-07-provider-push-http:http-push-connector:build
```

After the building end, you should verify that the connector jar is created in the directory
[http-push-connector.jar](http-push-connector/build/libs/http-push-connector.jar)

# How to run a connector

It is important to note that only the property file differs between the consumer and the provider.
You can find the configuration file in the directories below:

* [provider](http-push-provider/provider-configuration.properties)
* [consumer](http-push-consumer/consumer-configuration.properties)

The section bellow will show you some explanation about some of the properties that you can find in
the configuration files.

## Run the connectors

To run the provider, just run the following command

```bash
java -Dedc.keystore=transfer/transfer-07-provider-push-http/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.vault=transfer/transfer-07-provider-push-http/http-push-provider/provider-vault.properties -Dedc.fs.config=transfer/transfer-07-provider-push-http/http-push-provider/provider-configuration.properties -jar transfer/transfer-07-provider-push-http/http-push-connector/build/libs/push-connector.jar
```

To run the consumer, just run the following command

```bash
java -Dedc.keystore=transfer/transfer-07-provider-push-http/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.vault=transfer/transfer-07-provider-push-http/http-push-consumer/consumer-vault.properties -Dedc.fs.config=transfer/transfer-07-provider-push-http/http-push-consumer/consumer-configuration.properties -jar transfer/transfer-07-provider-push-http/http-push-connector/build/libs/push-connector.jar
```

Assuming you didn't change the ports in config files, the consumer will listen on the
ports `29191`, `29193` (management API) and `29194` (IDS API) and the provider will listen on the
ports `19191`, `19193` (management API) and `19194` (IDS API).

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
           "@context": {
             "edc": "https://w3id.org/edc/v0.0.1/ns/"
           },
           "@id": "http-pull-provider-dataplane",
           "url": "http://localhost:19192/control/transfer",
           "allowedSourceTypes": [ "HttpData" ],
           "allowedDestTypes": [ "HttpProxy", "HttpData" ],
           "properties": {
                "https://w3id.org/edc/v0.0.1/ns/publicApiUrl": "http://localhost:19291/public/"
            }
         }' \
     -X POST "http://localhost:19193/management/v2/dataplanes"
```

### 2. Create an Asset on the provider side

The provider connector needs to transfer a file to the location specified by the consumer connector
when the data are requested. In order to offer any data, the provider must maintain an internal list
of resources offered, through a contract offer, the so-called "catalog".

The following request creates an asset on the provider connector.

```bash
curl -d '{
           "@context": {
             "edc": "https://w3id.org/edc/v0.0.1/ns/"
           },
           "asset": {
             "@id": "assetId",
             "properties": {
               "name": "product description",
               "contenttype": "application/json"
             }
           },
           "dataAddress": {
             "name": "Test asset",
             "baseUrl": "https://jsonplaceholder.typicode.com/users",
             "type": "HttpData"
           }
         }' -H 'content-type: application/json' http://localhost:19193/management/v2/assets \
         -s | jq
```

> It is important to note that the `baseUrl` property of the `dataAddress` is a fake data used for
> the purpose of this example. It will be the data that the provider will push on the sample
> execution.

### 3. Create a Policy on the provider

In order to manage the accessibility rules of an asset, it is essential to create a policy. However,
to keep things simple, we will choose a policy that gives direct access to all the assets that are
associated within the contract definitions.
This means that the consumer connector can request any asset from the provider connector.

```bash
curl -d '{
           "@context": {
             "edc": "https://w3id.org/edc/v0.0.1/ns/",
             "odrl": "http://www.w3.org/ns/odrl/2/"
           },
           "@id": "aPolicy",
           "policy": {
             "@type": "set",
             "odrl:permission": [],
             "odrl:prohibition": [],
             "odrl:obligation": []
           }
         }' -H 'content-type: application/json' http://localhost:19193/management/v2/policydefinitions \
         -s | jq
```

### 4. Create a contract definition on Provider

To ensure an exchange between providers and consumers, the supplier must create a contract offer for
the good, on the basis of which a contract agreement can be negotiated. The contract definition
associates policies to a selection of assets to generate the contract offers that will be put in the
catalog. In this case, the selection is empty, so every asset is attached to these policies

```bash
curl -d '{
           "@context": {
             "edc": "https://w3id.org/edc/v0.0.1/ns/"
           },
           "@id": "1",
           "accessPolicyId": "aPolicy",
           "contractPolicyId": "aPolicy",
           "assetsSelector": []
         }' -H 'content-type: application/json' http://localhost:19193/management/v2/contractdefinitions \
         -s | jq
```

Sample output:

```json
{
  "createdAt": 1674578184023,
  "id": "1"
}
```

### 5. How to fetch catalog on consumer side

In order to offer any data, the consumer can fetch the catalog from the provider, that will contain
all the contract offers available for negotiation. In our case, it will contain a single contract
offer, the so-called "catalog". To get the catalog from the consumer side, you can use the following
endpoint:

```bash
curl -X POST "http://localhost:29193/management/v2/catalog/request" \
    -H 'Content-Type: application/json' \
    -d '{
      "@context": {
        "edc": "https://w3id.org/edc/v0.0.1/ns/"
      },
      "providerUrl": "http://localhost:19194/protocol",
      "protocol": "dataspace-protocol-http"
    }' -s | jq
```

Sample output:

```json
{
  "@id": "31f6d748-d35b-4dec-9e34-d141fd17b458",
  "@type": "dcat:Catalog",
  "dcat:dataset": {
    "@id": "assetId",
    "@type": "dcat:Dataset",
    "odrl:hasPolicy": {
      "@id": "MQ==:YXNzZXRJZA==:MzY3ZGZlYTgtYWI5OS00OWMwLThmNmYtM2Y2YmMxNGE1ZDc4",
      "@type": "odrl:Set",
      "odrl:permission": [],
      "odrl:prohibition": [],
      "odrl:obligation": [],
      "odrl:target": "assetId"
    },
    "dcat:distribution": [
      {
        "@type": "dcat:Distribution",
        "dct:format": {
          "@id": "HttpProxy"
        },
        "dcat:accessService": "2a5178c3-c937-4ac2-85be-c46dbc6c5642"
      },
      {
        "@type": "dcat:Distribution",
        "dct:format": {
          "@id": "HttpData"
        },
        "dcat:accessService": "2a5178c3-c937-4ac2-85be-c46dbc6c5642"
      }
    ],
    "edc:name": "product description",
    "edc:id": "assetId",
    "edc:contenttype": "application/json"
  },
  "dcat:service": {
    "@id": "2a5178c3-c937-4ac2-85be-c46dbc6c5642",
    "@type": "dcat:DataService",
    "dct:terms": "connector",
    "dct:endpointUrl": "http://localhost:19194/protocol"
  },
  "edc:participantId": "anonymous",
  "@context": {
    "dct": "https://purl.org/dc/terms/",
    "edc": "https://w3id.org/edc/v0.0.1/ns/",
    "dcat": "https://www.w3.org/ns/dcat/",
    "odrl": "http://www.w3.org/ns/odrl/2/",
    "dspace": "https://w3id.org/dspace/v0.8/"
  }
}
```

### 6. Negotiate a contract

In order to request any data, a contract gets negotiated, and an agreement is resulting has to be
negotiated between providers and consumers.

The consumer now needs to initiate a contract negotiation sequence with the provider. That sequence
looks as follows:

1. Consumer sends a contract offer to the provider (__currently, this has to be equal to the
   provider's offer!__)
2. Provider validates the received offer against its own offer
3. Provider either sends an agreement or a rejection, depending on the validation result
4. In case of successful validation, provider and consumer store the received agreement for later
   reference

Of course, this is the simplest possible negotiation sequence. Later on, both connectors can also
send counter offers in addition to just confirming or declining an offer.

```bash
curl -d '{
  "@context": {
    "edc": "https://w3id.org/edc/v0.0.1/ns/",
    "odrl": "http://www.w3.org/ns/odrl/2/"
  },
  "@type": "NegotiationInitiateRequestDto",
  "connectorId": "provider",
  "connectorAddress": "http://localhost:19194/protocol",
  "consumerId": "consumer",
  "providerId": "provider",
  "protocol": "dataspace-protocol-http",
  "offer": {
   "offerId": "MQ==:YXNzZXRJZA==:MzY3ZGZlYTgtYWI5OS00OWMwLThmNmYtM2Y2YmMxNGE1ZDc4",
   "assetId": "assetId",
   "policy": {
     "@id": "MQ==:YXNzZXRJZA==:MzY3ZGZlYTgtYWI5OS00OWMwLThmNmYtM2Y2YmMxNGE1ZDc4",
     "@type": "Set",
     "odrl:permission": [],
     "odrl:prohibition": [],
     "odrl:obligation": [],
     "odrl:target": "assetId"
   }
  }
}' -X POST -H 'content-type: application/json' http://localhost:29193/management/v2/contractnegotiations \
-s | jq
```

Sample output:

```json
{
  ...
  "@id": "254015f3-5f1e-4a59-9ad9-bf0e42d4819e",
  "edc:createdAt": 1685525281848,
  ...
}
```

### 7. Getting the contract agreement id

After calling the endpoint for initiating a contract negotiation, we get a UUID as the response.
This UUID is the ID of the ongoing contract negotiation between consumer and provider. The
negotiation sequence between provider and consumer is executed asynchronously in the background by a
state machine. Once both provider and consumer either reach the `confirmed` or the  `declined`
state, the negotiation is finished. We can now use the UUID to check the current status of the
negotiation using an endpoint on the consumer side.

```bash
curl -X GET "http://localhost:29193/management/v2/contractnegotiations/<contract negotiation id, returned by the negotiation call>" \
    --header 'Content-Type: application/json' \
    -s | jq
```

Sample output:

```json
{
  "@type": "edc:ContractNegotiationDto",
  "@id": "5ca21b82-075b-4682-add8-c26c9a2ced67",
  "edc:type": "CONSUMER",
  "edc:protocol": "dataspace-protocol-http",
  "edc:state": "FINALIZED",
  "edc:counterPartyAddress": "http://localhost:19194/protocol",
  "edc:callbackAddresses": [],
  "edc:contractAgreementId": "MQ==:YXNzZXRJZA==:MjQ2ODMxOTMtZmFhMS00MzMxLWE2YzYtYTQ1ZjNkNzJkYWNk",
  "@context": {
    "dct": "https://purl.org/dc/terms/",
    "edc": "https://w3id.org/edc/v0.0.1/ns/",
    "dcat": "https://www.w3.org/ns/dcat/",
    "odrl": "http://www.w3.org/ns/odrl/2/",
    "dspace": "https://w3id.org/dspace/v0.8/"
  }
}
```

### 8. Start the transfer

As a pre-requisite, you need to have an http server that runs on port 4000 and logs all the incoming requests, it will
be mandatory to get the data from the provider.

```bash
./gradlew util:http-request-logger:build
HTTP_SERVER_PORT=4000 java -jar util/http-request-logger/build/libs/http-request-logger.jar
```

Now that we have a contract agreement, we can finally request the file. In the request body, we need
to specify which asset we want transferred, the ID of the contract agreement, the address of the
provider connector and where we want the file transferred. You will find the request body below.
Before executing the request, insert the contract agreement ID from the previous step. Then run :

> keep in mind that, to make a transfer with a provider push method, the dataDestination type should
> be any value different from the "HttpProxy".

```bash
curl -X POST "http://localhost:29193/management/v2/transferprocesses" \
    -H "Content-Type: application/json" \
    -d '{
        "@context": {
          "edc": "https://w3id.org/edc/v0.0.1/ns/"
        },
        "@type": "TransferRequestDto",
        "connectorId": "provider",
        "connectorAddress": "http://localhost:19194/protocol",
        "contractId": "<contract agreement id>",
        "assetId": "assetId",
        "protocol": "dataspace-protocol-http",
        "dataDestination": { 
          "type": "HttpData",
          "baseUrl": "http://localhost:4000/api/consumer/store"
        }
    }' \
    -s | jq
```

Then, we will get a UUID in the response. This time, this is the ID of the `TransferProcess` (
process id) created on the consumer
side, because like the contract negotiation, the data transfer is handled in a state machine and
performed asynchronously.

Sample output:

```json
 {
  ...
  "@id": "591bb609-1edb-4a6b-babe-50f1eca3e1e9",
  "edc:createdAt": 1674078357807,
  ...
}
```

### 9. Check the transfer status

Due to the nature of the transfer, it will be very fast and most likely already done by the time you
read the UUID.

```bash
curl http://localhost:29193/management/v2/transferprocesses/<transfer process id>
```

### 10. Check the data

At this step, you can check the data by checking the log of the http server exposed on port 4000, you should see a log
that shows the same data that you can get from https://jsonplaceholder.typicode.com/users
