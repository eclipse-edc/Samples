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
./gradlew transfer:transfer-06-consumer-pull-http:http-pull-connector:build
```

After the build end you should verify that the connector jar is created in the directory
[http-pull-connector.jar](http-pull-connector/build/libs/http-pull-connector.jar)

# How to run a connector

It is important to note that only the property file differs between the consumer and the provider.
You can find the configuration file in the directories below:

* [provider](http-pull-provider/provider-configuration.properties)
* [consumer](http-pull-consumer/consumer-configuration.properties)

The section bellow will show you some explanation about some of the properties that you can find in
the configuration files.

#### 1. edc.receiver.http.endpoint

This property is used to define the endpoint where the connector consumer will send the
EndpointDataReference.

#### 2. edc.dataplane.token.validation.endpoint

This property is used to define the endpoint exposed by the control plane to validate the token.

## Run the connectors

To run the provider, just run the following command

```bash
java -Dedc.keystore=transfer/transfer-06-consumer-pull-http/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.vault=transfer/transfer-06-consumer-pull-http/http-pull-provider/provider-vault.properties -Dedc.fs.config=transfer/transfer-06-consumer-pull-http/http-pull-provider/provider-configuration.properties -jar transfer/transfer-06-consumer-pull-http/http-pull-connector/build/libs/pull-connector.jar
```

To run a consumer, just run the following command

```bash
java -Dedc.keystore=transfer/transfer-06-consumer-pull-http/certs/cert.pfx -Dedc.keystore.password=123456 -Dedc.vault=transfer/transfer-06-consumer-pull-http/http-pull-consumer/consumer-vault.properties -Dedc.fs.config=transfer/transfer-06-consumer-pull-http/http-pull-consumer/consumer-configuration.properties -jar transfer/transfer-06-consumer-pull-http/http-pull-connector/build/libs/pull-connector.jar
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
     -X POST "http://localhost:19193/management/v2/dataplanes" | -s | jq
```

### 2. Register data plane instance for consumer

The same thing that is done for the provider must be done for the consumer

```bash
curl -H 'Content-Type: application/json' \
     -d '{
           "@context": {
             "edc": "https://w3id.org/edc/v0.0.1/ns/"
           },
           "@id": "http-pull-consumer-dataplane",
           "url": "http://localhost:29192/control/transfer",
           "allowedSourceTypes": [ "HttpData" ],
           "allowedDestTypes": [ "HttpProxy", "HttpData" ],
           "properties": {
             "https://w3id.org/edc/v0.0.1/ns/publicApiUrl/publicApiUrl": "http://localhost:29291/public/"
            }
         }' \
             -X POST "http://localhost:29193/management/v2/dataplanes"
```

### 3. Create an Asset on the provider side

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
             "type": "HttpData",
             "name": "Test asset",
             "baseUrl": "https://jsonplaceholder.typicode.com/users",
             "proxyPath": "true"
           }
         }' -H 'content-type: application/json' http://localhost:19193/management/v2/assets \
         -s | jq
```

> It is important to note that the `baseUrl` property of the `dataAddress` is a fake data used for
> the purpose of this example. It will be the data that the consumer will pull on the sample
> execution.

Additional properties on `HttpData` can be used to allow consumers to enrich the data request:

- `proxyPath`: allows specifying additional path segments.
- `proxyQueryParams`: allows specifying query params.
- `proxyBody`: allows attaching a body.
- `proxyMethod`: allows specifying the Http Method (default `GET`)

### 4. Create a Policy on the provider

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

### 5. Create a contract definition on Provider

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
  ...
  "@id": "1",
  "edc:createdAt": 1674578184023,
  ...
}
```

### 6. How to fetch catalog on consumer side

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
      "@id": "MQ==:YXNzZXRJZA==:YTc4OGEwYjMtODRlZi00NWYwLTgwOWQtMGZjZTMwMGM3Y2Ey",
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

### 7. Negotiate a contract

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
   "offerId": "MQ==:YXNzZXRJZA==:YTc4OGEwYjMtODRlZi00NWYwLTgwOWQtMGZjZTMwMGM3Y2Ey",
   "assetId": "assetId",
   "policy": {
     "@id": "MQ==:YXNzZXRJZA==:YTc4OGEwYjMtODRlZi00NWYwLTgwOWQtMGZjZTMwMGM3Y2Ey",
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

### 8. Getting the contract agreement id

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
  "edc:contractAgreementId": "MQ==:YXNzZXRJZA==:YTc4OGEwYjMtODRlZi00NWYwLTgwOWQtMGZjZTMwMGM3Y2Ey",
  "@context": {
    "dct": "https://purl.org/dc/terms/",
    "edc": "https://w3id.org/edc/v0.0.1/ns/",
    "dcat": "https://www.w3.org/ns/dcat/",
    "odrl": "http://www.w3.org/ns/odrl/2/",
    "dspace": "https://w3id.org/dspace/v0.8/"
  }
}
```

### 9. Start the transfer

As a pre-requisite, you need to have an http server that runs on port 4000 and logs all the incoming requests, it will
be mandatory to get the EndpointDataReference that will be used to get the data.

```bash
./gradlew util:http-request-logger:build
HTTP_SERVER_PORT=4000 java -jar util/http-request-logger/build/libs/http-request-logger.jar
```

Now that we have a contract agreement, we can finally request the file. In the request body, we need
to specify which asset we want transferred, the ID of the contract agreement, the address of the
provider connector and where we want the file transferred. You will find the request body below.
Before executing the request, insert the contract agreement ID from the previous step. Then run :

> the "HttpProxy" method is used for the consumer pull method, and it means that it will be up to
> the consumer to request the data to the provider and that the request will be a proxy for the
> datasource

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
          "type": "HttpProxy" 
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

### 10. Check the transfer status

Due to the nature of the transfer, it will be very fast and most likely already done by the time you
read the UUID.

```bash
curl http://localhost:29193/management/v2/transferprocesses/<transfer process id>
```


You should see the Transfer Process in `COMPLETED` state: 
```json
{
  ...
  "@id": "0f648d82-23b4-464d-8f7b-c89860efe7c9",
  "edc:state": "COMPLETED",
  ...
}

```

### 11. Pull the data

At this step, if you look at the http server logs, you will find a json representing the EndpointDataReference, needed
to get the data from the provider:

```json
{
  "id": "77a3551b-08da-4f81-b61d-fbc0c86c1069",
  "endpoint": "http://localhost:29291/public/",
  "authKey": "Authorization",
  "authCode": "eyJhbGciOiJSUzI1NiJ9.eyJkYWQiOiJ7XCJwcm9wZXJ0aWVzXCI6e1wiYXV0aEtleVwiOlwiQXV0aG9yaXphdGlvblwiLFwiYmFzZVVybFwiOlwiaHR0cDpcL1wvbG9jYWxob3N0OjE5MjkxXC9wdWJsaWNcL1wiLFwiYXV0aENvZGVcIjpcImV5SmhiR2NpT2lKU1V6STFOaUo5LmV5SmtZV1FpT2lKN1hDSndjbTl3WlhKMGFXVnpYQ0k2ZTF3aVltRnpaVlZ5YkZ3aU9sd2lhSFIwY0hNNlhDOWNMMnB6YjI1d2JHRmpaV2h2YkdSbGNpNTBlWEJwWTI5a1pTNWpiMjFjTDNWelpYSnpYQ0lzWENKdVlXMWxYQ0k2WENKVVpYTjBJR0Z6YzJWMFhDSXNYQ0owZVhCbFhDSTZYQ0pJZEhSd1JHRjBZVndpZlgwaUxDSmxlSEFpT2pFMk56UTFPRGcwTWprc0ltTnBaQ0k2SWpFNk1XVTBOemc1TldZdE9UQXlOUzAwT1dVeExUazNNV1F0WldJNE5qVmpNemhrTlRRd0luMC5ITFJ6SFBkT2IxTVdWeWdYZi15a0NEMHZkU3NwUXlMclFOelFZckw5eU1tQjBzQThwMHFGYWV0ZjBYZHNHMG1HOFFNNUl5NlFtNVU3QnJFOUwxSE5UMktoaHFJZ1U2d3JuMVhGVUhtOERyb2dSemxuUkRlTU9ZMXowcDB6T2MwNGNDeFJWOEZoemo4UnVRVXVFODYwUzhqbU4wZk5sZHZWNlFpUVFYdy00QmRTQjNGYWJ1TmFUcFh6bDU1QV9SR2hNUGphS2w3RGsycXpJZ0ozMkhIdGIyQzhhZGJCY1pmRk12aEM2anZ2U1FieTRlZXU0OU1hclEydElJVmFRS1B4ajhYVnI3ZFFkYV95MUE4anNpekNjeWxyU3ljRklYRUV3eHh6Rm5XWmczV2htSUxPUFJmTzhna2RtemlnaXRlRjVEcmhnNjZJZzJPR0Eza2dBTUxtc3dcIixcInByb3h5TWV0aG9kXCI6XCJ0cnVlXCIsXCJwcm94eVF1ZXJ5UGFyYW1zXCI6XCJ0cnVlXCIsXCJwcm94eUJvZHlcIjpcInRydWVcIixcInR5cGVcIjpcIkh0dHBEYXRhXCIsXCJwcm94eVBhdGhcIjpcInRydWVcIn19IiwiZXhwIjoxNjc0NTg4NDI5LCJjaWQiOiIxOjFlNDc4OTVmLTkwMjUtNDllMS05NzFkLWViODY1YzM4ZDU0MCJ9.WhbTzERmM75mNMUG2Sh-8ZW6uDQCus_5uJPvGjAX16Ucc-2rDcOhAxrHjR_AAV4zWjKBHxQhYk2o9jD-9OiYb8Urv8vN4WtYFhxJ09A0V2c6lB1ouuPyCA_qKqJEWryTbturht4vf7W72P37ERo_HwlObOuJMq9CS4swA0GBqWupZHAnF-uPIQckaS9vLybJ-gqEhGxSnY4QAZ9-iwSUhkrH8zY2GCDkzAWIPmvtvRhAs9NqVkoUswG-ez1SUw5bKF0hn2OXv_KhfR8VsKKYUbKDQf5Wagk7rumlYbXMPNAEEagI4R0xiwKWVTfwwZPy_pYnHE7b4GQECz3NjhgdIw",
  "properties": {
    "cid": "1:1e47895f-9025-49e1-971d-eb865c38d540"
  }
}
```

Once this json is read, use a tool like postman or curl to execute the following query, to read the
data

```bash
curl --location --request GET 'http://localhost:29291/public/' --header 'Authorization: <auth code>'
```

At the end, and to be sure that you correctly achieved the pull, you can check if the data you get
is the same as the one you can get at https://jsonplaceholder.typicode.com/users


Since we configured the `HttpData` with `proxyPath`, we could also ask for a specific user with:

```bash
curl --location --request GET 'http://localhost:29291/public/1' --header 'Authorization: <auth code>'
```

And the data returned will be the same as in https://jsonplaceholder.typicode.com/users/1
