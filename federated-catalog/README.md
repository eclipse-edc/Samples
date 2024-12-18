# Federated Catalog Samples

The samples in this section focus on the topic of Federated Catalogs.

The Federated Catalog (FC) functions as an aggregated repository of
catalogs obtained from multiple
participants in the dataspace. To accomplish this, the FC utilizes crawlers
that periodically crawl the catalogs from each participant and store this list
of catalogs in a local cache.
By maintaining this locally cached version of catalogs, it eliminates the need to query
each participant individually, resulting in faster and more reliable queries. Refer to the 
[eclipse-edc/FederatedCatalog](https://github.com/eclipse-edc/FederatedCatalog) for further details.


The following samples shows how to
* implement, build and run different versions of FC e.g.
    * standalone,
    * embedded.
* implement TargetNodeDirectory and resolve Target Nodes,
    * from a static file containing all participants' DSP endpoints,

## Samples

### [FC sample 00](./fc-00-basic/README.md): Federated Catalog Prerequisites
The purpose of this example is to make preparations for implementing Federated Catalog (FC).
We'll set up a basic federated catalog that includes necessary dependencies for triggering the FC,
along with some other modules to demonstrate FC functionalities.

---

### Implement Different Versions of FC
### [FC sample 01](./fc-01-embedded/README.md): Implement an embedded federated catalog 
This sample demonstrates how we can implement a federated catalog which is embedded in a connector.
The connector exposes a catalog endpoint that serves the consolidated list of catalogs.
### [FC sample 02](./fc-02-standalone/README.md): Implement a standalone federated catalog

In this sample we focus on the implementation of
a standalone federated catalog. Unlike the previous sample,
a standalone federated catalog will not have the added functionalities of a connector. However, it also
exposes a catalog API that serves the list of catalogs.

---
### Different Implementations of Node Resolver

### [FC sample 03](./fc-03-static-node-directory/README.md): Resolve Target Catalog Nodes from static participant file
This sample demonstrates a Catalog Node resolver, that implements TargetNodeDirectory. It resolves the Target Catalog
Nodes from a static participant file containing the DSP endpoints of the participants.