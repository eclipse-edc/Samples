# Implement a simple "Provider Push" Http transfer flow

This sample demonstrates the "provider push" use case that you can find more details
on [Transfer data plane documentation](https://github.com/eclipse-edc/Connector/tree/main/extensions/control-plane/transfer/transfer-data-plane).

This samples consists of:

* Performing a file transfer
    * The consumer will initiate a file transfer
    * Provider Control Plane retrieves the DataAddress of the actual data source and creates a
      DataFlowRequest based on the received DataRequest and this data address
* Provider Data Plane fetches data from the actual data source
* Provider Data Plane pushes data to the consumer service

## Prerequisites

The following steps assume your provider and consumer connectors are still up and running and contract
negotiation has taken place successfully. Furthermore, the http server should be up as well.
If not, re-visit the [Prerequisites](../transfer-00-prerequisites/README.md)
and [Negotiation](../transfer-01-negotiation/README.md) chapters.

# Run the sample

Running this sample consists of multiple steps, that are executed one by one and following the same
order.

### 1. Start a http server

As a pre-requisite, you need to have a logging webserver that runs on port 4000 and logs all the incoming requests, the
data will be sent to this server.

```bash
docker build -t http-request-logger util/http-request-logger
docker run -p 4000:4000 http-request-logger
```

### 2. Start the transfer

Before executing the request, modify the [request body](resources/start-transfer.json) by inserting the contract agreement ID
from the [Negotiation](../transfer-01-negotiation/README.md) chapter. 
You can re-use the same asset, policies and contract negotiation from before.

```bash
curl -X POST "http://localhost:29193/management/v3/transferprocesses" \
    -H "Content-Type: application/json" \
    -d @transfer/transfer-02-provider-push/resources/start-transfer.json \
    -s | jq
```
> keep in mind that, to make a transfer with a provider push method, the dataDestination type should
> be any value different from the "HttpProxy".

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
curl http://localhost:29193/management/v3/transferprocesses/<transfer process id>
```

Notice the transfer COMPLETED state

### 4. Check the data

At this step, you can check the data by checking the log of the http server exposed on port 4000, you should see a log
that shows the same data that you can get from https://jsonplaceholder.typicode.com/users.

[Next Chapter](../transfer-03-consumer-pull/README.md)
