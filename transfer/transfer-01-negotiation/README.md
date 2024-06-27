# Implement a negotiation workflow between two connectors


Before two connectors can exchange actual data, negotiation has to take place.

The final goal of this example is to showcase the negotiation workflow between two connectors so that
the actual data transfer can take place. The actual data transfer will be part of the next chapters.

You will:

* Create an asset on the provider (the asset will be the data to be shared)
* Create an access policy on the provider (the policy will define the access right to the data)
* Create a contract definition on the provider

At this step, the consumer should be able to fetch the catalog from the provider and to see the
contract offer generated from the resources that have been created.

Once the catalog is available, to access the data, the consumer should follow the following steps:

* Perform a contract negotiation with the provider

> For the sake of simplicity, we will use an in-memory catalog and fill it with just one single
> asset. This will be deleted after the provider shutdown.

The provider connector is the one managing assets and respective access policies for which a contract negotiation
can be executed while the consumer is the one "requesting" assets and initiating contract negotiations.

## Run the sample

Running this sample consists of multiple steps, that are executed one by one and following the same
order.

The following steps assume your provider and consumer connector are still up and running.
If not, re-run them as described in the [Prerequisites](../transfer-00-prerequisites/README.md) chapter.

### 1. Create an Asset on the provider side

The provider connector needs to transfer a file to the location specified by the consumer connector
when the data are requested. In order to offer any data, the provider must maintain an internal list
of resources offered, through a contract offer, the so-called "catalog".

The following [request](resources/create-asset.json) creates an asset on the provider connector.

```bash
curl -d @transfer/transfer-01-negotiation/resources/create-asset.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/assets \
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

### 2. Create a Policy on the provider

In order to manage the accessibility rules of an asset, it is essential to create a policy. However,
to keep things simple, we will choose a policy that gives direct access to all the assets that are
associated within the contract definitions.
This means that the consumer connector can request any asset from the provider connector.

```bash
curl -d @transfer/transfer-01-negotiation/resources/create-policy.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/policydefinitions \
  -s | jq
```

### 3. Create a contract definition on Provider

To ensure an exchange between provider and consumer, the provider must create a contract offer for
the asset, on the basis of which a contract agreement can be negotiated. The contract definition
associates policies to a selection of assets to generate the contract offers that will be put in the
catalog. In this case, the selection is empty, so every asset is attached to these policies.

```bash
curl -d @transfer/transfer-01-negotiation/resources/create-contract-definition.json \
  -H 'content-type: application/json' http://localhost:19193/management/v3/contractdefinitions \
  -s | jq

```

Sample output:

```json
{
  ...
  "@id": "1",
  "createdAt": 1674578184023,
  ...
}
```

### 4. How to fetch catalog on consumer side

In order to request any data, the consumer must fetch the catalog from the provider, which contains
all the contract offers available for negotiation. In our case, it will contain a single contract
offer, the so-called "catalog". To get the catalog from the consumer side, you can use the following
request:

```bash
curl -X POST "http://localhost:29193/management/v3/catalog/request" \
    -H 'Content-Type: application/json' \
    -d @transfer/transfer-01-negotiation/resources/fetch-catalog.json -s | jq
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
    "name": "product description",
    "id": "assetId",
    "contenttype": "application/json"
  },
  "dcat:service": {
    "@id": "2a5178c3-c937-4ac2-85be-c46dbc6c5642",
    "@type": "dcat:DataService",
    "dct:terms": "connector",
    "dct:endpointUrl": "http://localhost:19194/protocol"
  },
  "participantId": "anonymous",
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
    "dct": "https://purl.org/dc/terms/",
    "dcat": "https://www.w3.org/ns/dcat/",
    "odrl": "http://www.w3.org/ns/odrl/2/",
    "dspace": "https://w3id.org/dspace/v0.8/"
  }
}
```

### 5. Negotiate a contract

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
send counteroffers in addition to just confirming or declining an offer.

Please replace the `{{contract-offer-id}}` placeholder in the [`negotiate-contract.json`](resources/negotiate-contract.json)
file with the contract offer id you found in the catalog at the path `dcat:dataset.odrl:hasPolicy.@id`.

```bash
curl -d @transfer/transfer-01-negotiation/resources/negotiate-contract.json \
  -X POST -H 'content-type: application/json' http://localhost:29193/management/v3/contractnegotiations \
  -s | jq
```

Sample output:

```json
{
  ...
  "@id": "254015f3-5f1e-4a59-9ad9-bf0e42d4819e",
  "createdAt": 1685525281848,
  ...
}
```

### 6. Getting the contract agreement id

After calling the endpoint for initiating a contract negotiation, we get a UUID as the response.
This UUID is the ID of the ongoing contract negotiation between consumer and provider. The
negotiation sequence between provider and consumer is executed asynchronously in the background by a
state machine. Once both provider and consumer either reach the `confirmed` or the  `declined`
state, the negotiation is finished. We can now use the UUID to check the current status of the
negotiation using an endpoint on the consumer side.

```bash
curl -X GET "http://localhost:29193/management/v3/contractnegotiations/{{contract-negotiation-id}}" \
    --header 'Content-Type: application/json' \
    -s | jq
```

Sample output:

```json
{
  "@type": "ContractNegotiation",
  "@id": "5ca21b82-075b-4682-add8-c26c9a2ced67",
  "type": "CONSUMER",
  "protocol": "dataspace-protocol-http",
  "state": "FINALIZED",
  "counterPartyAddress": "http://localhost:19194/protocol",
  "callbackAddresses": [],
  "contractAgreementId": "0b3150be-feaf-43bc-91e1-90f050de28bd",  <---------
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
    "dct": "https://purl.org/dc/terms/",
    "dcat": "https://www.w3.org/ns/dcat/",
    "odrl": "http://www.w3.org/ns/odrl/2/",
    "dspace": "https://w3id.org/dspace/v0.8/"
  }
}
```
At this point contract negotiation has been successfully completed.
The connectors are now ready to enter the data transfer phase.

Note down the `contractAgreementId`. You will need it in the next chapters.

[Next Chapter](../transfer-02-consumer-pull/README.md)
