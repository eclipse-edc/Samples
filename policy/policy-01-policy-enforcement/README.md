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

In this extension, we'll define the policy and create the provider's offer. In addition, we'll implement and register
a function to evaluate the defined policy.

### Creating the provider offer

In order for the provider to offer any data, we need to create 3 things: an `Asset` (= what data should be offered),
a `PolicyDefinition` (= under which conditions should data be offered), and a `ContractDefinition`, that links the
`Asset` and `PolicyDefinition`.

We'll start with creating the `PolicyDefinition`, which contains a `Policy` and an ID. Each `Policy` needs to contain
at least one rule describing which actions are allowed, disallowed or required to perform. Each rule can optionally
contain a set of constraints that further refine the actions. For more information on the policy model take a look at
the [documentation](https://github.com/eclipse-edc/Connector/blob/main/docs/developer/architecture/usage-control/policies.md)
or the [policy section in the developer handbook](https://github.com/eclipse-edc/docs/blob/main/developer/handbook.md#policies).

For our example, we create a `Permission` with action type `USE`, as we want to allow the usage of our offered data.
But we only want to allow the usage under the condition that the requesting participant is in a certain location,
therefore we define a constraint. In that constraint we state that the participant's location has to be equal to `eu`.
We then add the constraint to our permission and the permission to our policy. Lastly, we give an ID to our policy to
create the `PolicyDefinition`:

```java
var locationConstraint = AtomicConstraint.Builder.newInstance()
        .leftExpression(new LiteralExpression(LOCATION_CONSTRAINT_KEY))
        .operator(Operator.EQ)
        .rightExpression(new LiteralExpression("eu"))
        .build();
var permission = Permission.Builder.newInstance()
        .action(Action.Builder.newInstance()
            .type("USE")
            .build())
        .constraint(locationConstraint)
        .build();
var policyDefinition = PolicyDefinition.Builder.newInstance()
        .id(LOCATION_POLICY_ID)
        .policy(Policy.Builder.newInstance()
            .type(PolicyType.SET)
            .permission(permission)
            .build())
        .build();
        
policyDefinitionStore.create(policyDefinition);
```

Then, we create an `Asset` with a `DataAddress` of type *test*. This asset will **not** work for a data transfer,
as *test* is not an actual transfer type. But, as we're not going to transfer any data in this sample, this is
sufficient for our example. The last thing we create is a `ContractDefinition` that references the previously created
policy definition and asset. With this, the provider offers the asset under the condition that the
requesting participant is located in the EU.

### Creating rule bindings

The provider now offers an asset with a policy that imposes a constraint, but if we were to run the sample now,
we would not see any policy evaluation happening. This is because the EDC do not regard any rules or constraints
for evaluation unless we configure it. The EDC use the concept of *policy scopes* to define which rules and constraints
should be evaluated in certain runtime contexts, as some rules or constraints may only make sense in some contexts,
but not in others. A simple example is a rule that states *data must be anonymized*. Evaluating this during the
contract negotiation would not make much sense, as at this point in time no data is being exchanged yet and therefore
nothing can be anonymized. So we need to define which rules and constraints should be evaluated in which scopes.
This is done by creating *rule bindings* at the `RuleBindingRegistry`. For our example, we create the following rule
bindings:

```java
ruleBindingRegistry.bind("USE", ALL_SCOPES);
ruleBindingRegistry.bind(LOCATION_CONSTRAINT_KEY, NEGOTIATION_SCOPE);
```

When creating a rule binding, we can bind an action type or constraint to either all scopes or just a specific one.
Here, we bind the action type `USE` to all scopes, so that rules with this action type are always evaluated. For the
location constraint we choose the negotiation scope, meaning it will only be evaluated during the contract negotiation.
Information on available scopes can be found [here](https://github.com/eclipse-edc/Connector/blob/main/docs/developer/policy-engine.md).

### Implementing the function for evaluation

With the rule bindings in place, the provider will now try to evaluate our policy including the constraint during a
contract negotiation, but it does not yet know *how* to evaluate this constraint. For this, we need to implement a
function, for which the EDC offer two interfaces: `AtomicConstraintFunction` and `RuleFunction`. The former is meant
for evaluating a single constraint of a rule, while is latter is meant for evaluating a complete rule node (including
constraints as well as duties that may be associated with a permission). For our example, we choose to implement an
`AtomicConstraintFunction`, as we want to evaluate our location constraint:

```java
public class LocationConstraintFunction implements AtomicConstraintFunction<Permission> {

    //...
    
    @Override
    public boolean evaluate(Operator operator, Object rightValue, Permission rule, PolicyContext context) {
        var region = context.getParticipantAgent().getClaims().get("region");

        monitor.info(format("Evaluating constraint: location {} {}", operator, rightValue.toString()));
    
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
`AtomicConstraintFunction` we get the constraint's operator and right value as well as the containing rule node and
a `PolicyContext` as parameters. Using these, we have to determine whether the constraint is fulfilled. Since we want
to check the requesting participant's location, we need to access information about the participant. This is supplied
through the `PolicyContext`. We get the participant's claim with key *region* to obtain information about the
participant's location. We can then compare the location to the expected value depending on the operator used. The
function should return true, if the constraint is fulfilled, and false otherwise.

**Note**: we can use the *region* claim here because our connectors use the `iam-mock` extension, which always adds
a claim with this exact name to all tokens. Depending on the identity provider used, different claims may be present,
or the same claim may have a different name.

### Registering the function with the policy engine

After creating our function for evaluation, the last thing we need to do is register this function at the
`PolicyEngine`, so that it is available for evaluation:

```java
policyEngine.registerFunction(NEGOTIATION_SCOPE, Permission.class, LOCATION_CONSTRAINT_KEY, new LocationConstraintFunction(monitor));
```

When registering the function, we again have to specify a scope. This allows for evaluating the same rule or
constraint differently in different runtime contexts. Since we bound our constraint to the negotiation scope, we also
register our function for that scope. Next, we need to specify the type of rule our function should be used for. This
is important, as the same constraint may have different implications as part of a permission, prohibition or duty.
When registering an `AtomicConstraintFunction`, we also have to specify a key that the function is associated with.
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
* `libs.edc.dsp`: enables connector-to-connector communication via the Dataspace Protocol
* `libs.edc.iam.mock`: mocks an identity provider

**Note**: we do not include any `data-plane` modules, as we are not going to transfer any data in this sample. To
be able to actually transfer data, additional dependencies are required. More information can be found in the
[documentation](https://github.com/eclipse-edc/Connector/blob/main/docs/developer/build-your-own-connector.md) and in
the [transfer samples](../../transfer/README.md).

#### Provider

For the provider, we also add a dependency on our previously created `policy-functions` extension, so that it offers
our asset with the location restricted policy and is able to enforce the latter.

#### Consumer

In addition, we add the following dependency on the consumer side, as we will use the management API to initiate a
contract negotiation between provider and consumer:

* `libs.edc.management.api`: provides the API for interacting with the control-plane

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

### 2. Start a contract negotiation

To start the contract negotiation between provider and consumer, we'll use an endpoint of the consumer's management API.
In the request body for this request, we need to provide information about which connector we want to negotiate with,
which protocol to use and which offer we want to negotiate. The request body is prepared in
[contractoffer.json](./contractoffer.json). To start the negotiation, run the following command:

```bash
curl -X POST -H "Content-Type: application/json" -d @policy/policy-01-policy-enforcement/contractoffer.json "http://localhost:9192/api/management/v2/contractnegotiations"
```

You'll get back a UUID. This is the ID of the contract negotiation process which is being asynchronously executed
in the background.

### 3. Get the contract negotiation state

Using the ID received in the previous step, we can now view the state of the negotiation by calling another endpoint
of the consumer's data management API:

```bash
curl -X GET "http://localhost:9192/api/management/v2/contractnegotiations/<UUID>"
```

In the response we'll get a description of the negotiation, similar to the following:

```json
{
  ...
  "edc:contractAgreementId": null,
  "edc:state": "DECLINED",
  ...
}
```

We can see that the negotiation has been declined, and we did not receive a contract agreement. If we now take a look
at the provider's logs, we'll see the following lines:

```bash
[TODO: copy provider logs showing the failed policy evaluation]
```

The consumer was not able to get a contract agreement, because it does not fulfil the location-restricted policy. This
means we have successfully implemented and configured the policy evaluation on provider side. Building up on this
example, you can now tackle more complex policies, by e.g. defining and combining different constraints and creating
the respective functions for evaluation.

## Sample variations

You can play around with this sample a bit and run it in different variations, yielding different outcomes. Some
possible variations are described in the following.

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
ruleBindingRegistry.bind("USE", ALL_SCOPES);
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
//ruleBindingRegistry.bind("USE", ALL_SCOPES);
ruleBindingRegistry.bind(LOCATION_CONSTRAINT_KEY, NEGOTIATION_SCOPE);
```
