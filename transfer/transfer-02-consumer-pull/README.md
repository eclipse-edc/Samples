# Implement a simple "Consumer Pull" Http transfer flow

The purpose of this sample is to show a data exchange between two connectors, one representing the
data provider and the other, the consumer. It's based on a "consumer pull" use case that you can find
more details
on [Transfer data plane documentation](https://github.com/eclipse-edc/Connector/tree/main/extensions/control-plane/transfer/transfer-data-plane).

This sample consists of the following steps:

* Perform a file transfer initiated by the consumer
* The provider will send an EndpointDataReference to the consumer
* The consumer will call the endpoint and fetch the data

## Prerequisites

The following steps assume your provider and consumer connectors are still up and running and contract
negotiation has taken place successfully.
If not, re-visit the [Prerequisites](../transfer-00-prerequisites/README.md)
and [Negotiation](../transfer-01-negotiation/README.md) chapters.

# Run the sample

Running this sample consists of multiple steps, that are executed one by one and following the same
order.

### 1. Start a http server

As a pre-requisite, you need to have a logging webserver that runs on port 4000 and logs all the incoming requests, it will
be mandatory to get the EndpointDataReference that will be used to get the data.

```bash
docker build -t http-request-logger util/http-request-logger
docker run -p 4000:4000 http-request-logger
```

### 2. Start the transfer

In the [request body](resources/start-transfer.json), we need to specify which asset we want transferred, the ID of the contract agreement, the address of the
provider connector and where we want the file transferred.
Before executing the request, insert the `contractAgreementId` from the previous chapter. Then run:

```bash
curl -X POST "http://localhost:29193/management/v2/transferprocesses" \
  -H "Content-Type: application/json" \
  -d @transfer/transfer-02-consumer-pull/resources/start-transfer.json \
  -s | jq

```

> the "HttpProxy" method is used for the consumer pull method, and it means that it will be up to
> the consumer to request the data to the provider and that the request will be a proxy for the
> datasource

Then, we will get a UUID in the response. This time, this is the ID of the `TransferProcess` (
process id) created on the consumer
side, because like the contract negotiation, the data transfer is handled in a state machine and
performed asynchronously.

Sample output:

```json
{
  ...
  "@id": "591bb609-1edb-4a6b-babe-50f1eca3e1e9",
  "createdAt": 1674078357807,
  ...
}
```

### 3. Check the transfer status

Due to the nature of the transfer, it will be very fast and most likely already done by the time you
read the UUID.

```bash
curl http://localhost:29193/management/v2/transferprocesses/<transfer process id>
```

You should see the Transfer Process in `STARTED` state: 

```json
{
  ...
  "@id": "591bb609-1edb-4a6b-babe-50f1eca3e1e9",
  "state": "STARTED",
  ...
}

```

> Note that for the consumer pull scenario the TP will stay in STARTED state after the data has been transferred successfully.
> It might get eventually get shifted to TERMINATED or DEPROVISIONED by other resources, but this is not scope of this sample.

### 4. Check the data

At this step, if you look at the http server logs, you will find a json representing the EndpointDataReference, needed
to get the data from the provider:

```json
{
  "id": "591bb609-1edb-4a6b-babe-50f1eca3e1e9",
  "endpoint": "http://localhost:29291/public/",
  "authKey": "Authorization",
  "authCode": "{{auth-code}}",
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

Your first data transfer has been completed successfully.
Continue with the [next chapter](../transfer-03-provider-push/README.md) to run through a "provider push" scenario.
