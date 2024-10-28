# Policy enforcement

In this sample we'll learn how to enable policy enforcement. As the EDC are a framework, they do not provide any
evaluation out-of-the-box, but instead provide an evaluation system that can be easily configured to suit custom needs.
We'll perform the necessary configurations and implement and register a function for evaluating a policy.

We will set up two connectors, a provider and a consumer, and let the provider offer an asset with a policy that
imposes a location restriction. So depending on the consumer's location, the consumer will be able to negotiate a
contract for requesting the asset or not. The sample consists of multiple modules:

* `policy-functions`: creates the provider's offer and provides the function for policy enforcement
* `[policy-enforcement-provider|consumer]`: contains the build and config files for the respective connector

## Creating the policy functions extension

In this extension, we'll implement and register a function to evaluate the location-restricted policy we will create
later.

### Creating rule bindings

In this sample, the provider will offer an asset with a policy that imposes a constraint, but if we were to run the
sample now, we would not see any policy evaluation happening. This is because the EDC do not regard any rules or
constraints for evaluation unless we configure it. The EDC use the concept of *policy scopes* to define which rules
and constraints should be evaluated in certain runtime contexts, as some rules or constraints may only make sense in
some contexts, but not in others. A simple example is a rule that states *data must be anonymized*. Evaluating this
during the contract negotiation would not make much sense, as at this point in time no data is being exchanged yet
and therefore nothing can be anonymized. So we need to define which rules and constraints should be evaluated in which
scopes. This is done by creating *rule bindings* at the `RuleBindingRegistry`. For our example, we create the following
rule bindings:

```java
ruleBindingRegistry.bind("use", ALL_SCOPES);
ruleBindingRegistry.bind(LOCATION_CONSTRAINT_KEY, NEGOTIATION_SCOPE);
```

When creating a rule binding, we can bind an action type or constraint to either all scopes or just a specific one.
Here, we bind the action type `use` to all scopes, so that rules with this action type are always evaluated. For the
location constraint we choose the negotiation scope, meaning it will only be evaluated during the contract negotiation.
Information on available scopes can be found
[here](https://eclipse-edc.github.io/documentation/for-adopters/control-plane/policy-engine/).

### Implementing the function for evaluation

With the rule bindings in place, the provider will now try to evaluate our policy including the constraint during a
contract negotiation, but it does not yet know *how* to evaluate this constraint. For this, we need to implement a
function, for which the EDC offer two interfaces: `AtomicConstraintRuleFunction` and `PolicyRuleFunction`. The former is
meant for evaluating a single constraint of a rule, while is latter is meant for evaluating a complete rule node 
(including constraints as well as duties that may be associated with a permission). For our example, we choose to 
implement an `AtomicConstraintRuleFunction`, as we want to evaluate our location constraint:

```java
public class LocationConstraintFunction implements AtomicConstraintRuleFunction<Permission, ContractNegotiationPolicyContext> {
    
    //...
    
    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, ContractNegotiationPolicyContext context) {
        var region = context.participantAgent().getClaims().get("region");
        
        monitor.info(format("Evaluating constraint: location %s %s", operator, rightValue.toString()));
        
        return switch (operator) {
            case EQ -> Objects.equals(region, rightValue);
            case NEQ -> !Objects.equals(region, rightValue);
            case IN -> ((Collection<?>) rightValue).contains(region);
            default -> false;
        };
    }
}
```

When implementing either of the function interfaces, we have to override the `evaluate` method. For the
`AtomicConstraintRuleFunction` we get the constraint's operator and right value as well as the containing rule node and
an extension of `PolicyContext` as parameters, determined by the policy scope on which the function the function will be
registered (keep in mind that policy scopes and contexts are strictly bound to each other). Using these, we have to
determine whether the constraint is fulfilled. Since we want to check the requesting participant's location, we need to
access information about the participant. This is supplied through the context. We get the participant's claim with key
*region* to obtain information about the participant's location. We can then compare the location to the expected value
depending on the operator used. The function should return true, if the constraint is fulfilled, and false otherwise.

**Note**: we can use the *region* claim here because our connectors use the `iam-mock` extension, which always adds
a claim with this exact name to all tokens. Depending on the identity provider used, different claims may be present,
or the same claim may have a different name.

### Registering the function with the policy engine

After creating our function for evaluation, the last thing we need to do is register this function at the
`PolicyEngine`, so that it is available for evaluation:

```java
policyEngine.registerFunction(ContractNegotiationPolicyContext.class, Permission.class, LOCATION_CONSTRAINT_KEY, new LocationConstraintFunction(monitor));
```

When registering the function, we again have to specify a context class. This allows for evaluating the same rule or
constraint differently in different runtime contexts. Since we bound our constraint to the negotiation scope, we also
register our function for that scope. Next, we need to specify the type of rule our function should be used for. This
is important, as the same constraint may have different implications as part of a permission, prohibition or duty.
When registering an `AtomicConstraintRuleFunction`, we also have to specify a key that the function is associated with.
This has to resolve to exactly the constraint's left operand, so that the correct function for evaluation of a
constraint can be chosen depending on its left operand. So we set the key to the same value we used as our constraint's
left operand. And lastly, we hand over an instance of our function.

Now, during a contract negotiation, our provider will evaluate our constraint by calling our function's `evaluate`
method.

## Configuring the connectors

Next, let's configure the two connectors. For each connector we need a build file and a configuration file.

### Build files

In the build file, we define the following dependencies for both connectors:

* `libs.edc.control.plane.core`: the core module for the control-plane
* `libs.edc.configuration.filesystem`: enables configuration via a properties file
* `libs.edc.management.api`: provides the API for interacting with the control-plane
* `libs.edc.dsp`: enables connector-to-connector communication via the Dataspace Protocol
* `libs.edc.iam.mock`: mocks an identity provider

**Note**: we do not include any `data-plane` modules, as we are not going to transfer any data in this sample. To
be able to actually transfer data, additional dependencies are required. More information can be found in the
[documentation](https://github.com/eclipse-edc/Connector/blob/main/docs/developer/build-your-own-connector.md) and in
the [transfer samples](../../transfer/README.md).

#### Provider

For the provider, we also add a dependency on our previously created `policy-functions` extension, so that it is able
to enforce a policy rule with a location constraint.

### Configuration files

We create the `config.properties` files for both provider and consumer and first define their API bindings. We then
define the DSP callback addresses, which are required for callback during the contract negotiation, as well as their
participant IDs.

#### Consumer

For the consumer we also add the following property:

```properties
edc.mock.region=us
```

This defines the value for the consumer's region claim issued by the mock identity provider, which we use for evaluating
the consumer's location.

## Running the sample

Now, let's run the sample step by step.

### 1. Run connectors

First, we need to build and start both our connectors. Execute the following commands from the project root in two
separate terminal windows (one per connector):

Provider:
```bash
./gradlew policy:policy-01-policy-enforcement:policy-enforcement-provider:build
java -Dedc.fs.config=policy/policy-01-policy-enforcement/policy-enforcement-provider/config.properties -jar policy/policy-01-policy-enforcement/policy-enforcement-provider/build/libs/provider.jar
```

Consumer:
```bash
./gradlew policy:policy-01-policy-enforcement:policy-enforcement-consumer:build
java -Dedc.fs.config=policy/policy-01-policy-enforcement/policy-enforcement-consumer/config.properties -jar policy/policy-01-policy-enforcement/policy-enforcement-consumer/build/libs/consumer.jar
```

### 2. Create the provider's offer

In order for the provider to offer any data, we need to create 3 things: an `Asset` (= what data should be offered),
a `PolicyDefinition` (= under which conditions should data be offered), and a `ContractDefinition`, that links the
`Asset` and `PolicyDefinition`.

#### 2.1 Create the asset

We create an `Asset` with a `DataAddress` of type *test*. This asset will **not** work for a data transfer,
as *test* is not an actual transfer type. But, as we're not going to transfer any data in this sample, this is
sufficient for our example. You can view the request body for creating the asset in
[create-asset.json](./resources/create-asset.json). Run the following command to create the asset:

```bash
curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" \
  -d @policy/policy-01-policy-enforcement/resources/create-asset.json \
  "http://localhost:19193/management/v3/assets" | jq
```

#### 2.2 Create the policy definition

Next. we'll create the `PolicyDefinition`, which contains a `Policy` and an ID. Each `Policy` needs to contain
at least one rule describing which actions are allowed, disallowed or required to perform. Each rule can optionally
contain a set of constraints that further refine the actions. For more information on the policy model take a look at
the [documentation](https://github.com/eclipse-edc/Connector/blob/main/docs/developer/architecture/usage-control/policies.md)
or the [policy section in the developer handbook](https://github.com/eclipse-edc/docs/blob/main/developer/handbook.md#policies).

For our example, we create a `Permission` with action type `use`, as we want to allow the usage of our offered data.
But we only want to allow the usage under the condition that the requesting participant is in a certain location,
therefore we add a constraint to our permission. In that constraint we state that the participant's location has to
be equal to `eu`. You can view the request body for creating the policy definition in
[create-policy.json](./resources/create-policy.json). Run the following command to create the policy definition:

```bash
curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" \
  -d @policy/policy-01-policy-enforcement/resources/create-policy.json \
  "http://localhost:19193/management/v3/policydefinitions" | jq
```

#### 2.3 Create the contract definition

The last thing we create is a `ContractDefinition`, that references the previously created
policy definition and asset. We will set the policy both as the access and the contract policy in the contract
definition. To read up on the difference between the two, check out the
[developer handbook](https://github.com/eclipse-edc/docs/blob/main/developer/handbook.md#contract-definitions).
You can view the request body for creating the contract definition in
[create-contract-definition.json](./resources/create-contract-definition.json) Run the following command to create
the contract definition:

```bash
curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" \
  -d @policy/policy-01-policy-enforcement/resources/create-contract-definition.json \
  "http://localhost:19193/management/v3/contractdefinitions" | jq
```

With this, the provider now offers the asset under the condition that the requesting participant is located in the EU.

### 3. Make a catalog request

After starting both connectors, we'll first make a catalog request from the consumer to the provider to see the
provider's offers. For this, we'll use an endpoint of the consumer's management API, specifying the provider's address
in the request. The request body is prepared in [catalog-request.json](resources/catalog-request.json).

```bash
curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" \
  -d @policy/policy-01-policy-enforcement/resources/catalog-request.json \
  "http://localhost:29193/management/v3/catalog/request" | jq
```

We'll receive the following catalog in the response, where we can see the offer created in the provider's extension.

```json
{
  "@id": "4462b621-fb77-4e8c-91a5-8cbd85b967c2",
  "@type": "dcat:Catalog",
  "dcat:dataset": {
    "@id": "test-document",
    "@type": "dcat:Dataset",
    "odrl:hasPolicy": {
      "@id": "MQ==:dGVzdC1kb2N1bWVudA==:NjUzNTA5M2QtYTFjMi00YTRmLWE5NjYtYTM0ZjE2NjFjOTYy",
      "@type": "odrl:Set",
      "odrl:permission": {
        "odrl:target": "test-document",
        "odrl:action": {
          "odrl:type": "use"
        },
        "odrl:constraint": {
          "odrl:leftOperand": "location",
          "odrl:operator": {
            "@id": "odrl:eq"
          },
          "odrl:rightOperand": "eu"
        }
      },
      "odrl:prohibition": [],
      "odrl:obligation": [],
      "odrl:target": {
        "@id": "test-document"
      }
    },
    "dcat:distribution": [],
    "id": "test-document"
  },
  "dcat:service": {
    "@id": "fe9581ee-b4ec-473c-b0b7-96f30d957e87",
    "@type": "dcat:DataService",
    "dct:terms": "connector",
    "dct:endpointUrl": "http://localhost:8282/protocol"
  },
  "participantId": "provider",
  "@context": {
    "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
    "edc": "https://w3id.org/edc/v0.0.1/ns/",
    "dcat": "https://www.w3.org/ns/dcat/",
    "dct": "https://purl.org/dc/terms/",
    "odrl": "http://www.w3.org/ns/odrl/2/",
    "dspace": "https://w3id.org/dspace/v0.8/"
  }
}

```

But why are we able to see this offer, even though we set the location restricted policy as the access policy and our
consumer is not in the EU? While we did set the restricted policy as the access policy, we only bound the constraint to
the `NEGOTIATION_SCOPE` using the `RuleBindingRegistry`, meaning it will not be regarded for evaluations in the
cataloging phase.

We can now use the offer details received in the catalog to start a contract negotiation with the provider.

### 4. Start a contract negotiation

To start the contract negotiation between provider and consumer, we'll use an endpoint of the consumer's management API.
In the request body for this request, we need to provide information about which connector we want to negotiate with,
which protocol to use and which offer we want to negotiate. The request body is prepared in
[contractoffer.json](resources/contract-request.json). To start the negotiation, run the following command:

```bash
curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" \
  -d @policy/policy-01-policy-enforcement/resources/contract-request.json \
  "http://localhost:29193/management/v3/contractnegotiations" | jq
```

You'll get back a UUID. This is the ID of the contract negotiation process which is being asynchronously executed
in the background.

### 5. Get the contract negotiation state

Using the ID received in the previous step, we can now view the state of the negotiation by calling another endpoint
of the consumer's management API:

```bash
curl -X GET -H "X-Api-Key: password" "http://localhost:29193/management/v3/contractnegotiations/<UUID>" | jq
```

In the response we'll get a description of the negotiation, similar to the following:

```json
{
  ...
  "edc:contractAgreementId": null,
  "edc:state": "TERMINATED",
  ...
}
```

We can see that the negotiation has been declined, and we did not receive a contract agreement. If we now take a look
at the provider's logs, we'll see the following lines:

```bash
INFO 2024-02-12T11:07:32.954014912 Evaluating constraint: location EQ eu
DEBUG 2024-02-12T11:07:32.9562391 [Provider] Contract offer rejected as invalid: Policy eu-policy not fulfilled
```

The consumer was not able to get a contract agreement, because it does not fulfil the location-restricted policy. This
means we have successfully implemented and configured the policy evaluation on provider side. Building up on this
example, you can now tackle more complex policies, by e.g. defining and combining different constraints and creating
the respective functions for evaluation.

## Sample variations

You can play around with this sample a bit and run it in different variations, yielding different outcomes. Some
possible variations are described in the following.

**Note: the following variations do not build up on each other, so make sure to revert any changes done for one
variation before proceeding with the next!**

### Set consumer region to `eu`

Our policy requires the consumer to be in the EU. Change the property `edc.mock.region` in the consumer's
`config.properties` to the value `eu` and run the sample again. This time, the negotiation will reach the state
`FINALIZED` and reference a contract agreement, as our consumer now fulfils the policy.

```properties
edc.mock.region=eu
```

### Remove binding of constraint

In our `PolicyFunctionsExtension`, we've created a rule binding so that our constraint would be evaluated during the
contract negotiation. Remove this binding and run the sample again (while leaving the consumer's property
`edc.mock-region` with value `us`!). The negotiation will be confirmed and reference a contract agreement, even though
our consumer is not in the correct location. This happens, as without the binding of the constraint, the provider
will not regard it during evaluation.

```java
ruleBindingRegistry.bind("use", ALL_SCOPES);
//ruleBindingRegistry.bind(LOCATION_CONSTRAINT_KEY, NEGOTIATION_SCOPE);
```

### Remove binding of action type

In our `PolicyFunctionsExtension`, we've created rule bindings for our permission's action type as well as the
constraint. In the previous variation, we've removed the binding for the constraint. For this variation, we want to
leave the binding for the constraint in place, and instead remove the binding for the action type. Run the sample again
(while leaving the consumer's property `edc.mock-region` with value `us`!) and you will see the negotiation being
confirmed. Even though the constraint is bound to be evaluated and the consumer does not fulfil it, the constraint
is not evaluated and our function never called. This happens because there is no rule binding for the permission
containing the constraint, and thus the whole permission node is disregarded during evaluation.

```java
//ruleBindingRegistry.bind("use", ALL_SCOPES);
ruleBindingRegistry.bind(LOCATION_CONSTRAINT_KEY, NEGOTIATION_SCOPE);
```

### Bind the constraint to the cataloging scope

In our example, we've bound the constraint to the `NEGOTIATION_SCOPE`. Let's remove this binding and instead bind the
constraint as well as our function to the `CATALOGING_SCOPE` and rebuild the provider. When running a sample again,
you will not see the offer in the provider's catalog anymore. As the constraint is now evaluated during cataloging, the
offer is filtered out because our consumer does not fulfil the location constraint. Since the request body for the
negotiation is already prepared, you can still try to initiate a negotiation. Even though the constraint is not bound
to the negotiation scope anymore, the negotiation will be terminated. When receiving a request for a negotiation,
the provider will still evaluate its contract definitions' access policies using the catalog scope, to ensure that
a consumer cannot negotiate an offer it is not allowed to see.

```java
ruleBindingRegistry.bind(LOCATION_CONSTRAINT_KEY, CATALOGING_SCOPE);
policyEngine.registerFunction(CATALOGING_SCOPE, Permission.class, LOCATION_CONSTRAINT_KEY, new LocationConstraintFunction(monitor));
```
