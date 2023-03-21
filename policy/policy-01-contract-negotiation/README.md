# Create a policy function

This sample shows how a custom function for policy evaluation can be implemented and registered. For simplicity,
we will use just one connector, which initiates a contract negotiation with itself. The sample contains two modules:

- `policy-contract-negotiation-connector`: contains the connector configuration
- `policy-contract-negotiation-policy-functions`: provides the function for policy evaluation

## Creating and registering the function

The [`TimeIntervalFunction`](policy/policy-01-contract-negotiation/policy-contract-negotiation-policy-functions/src/main/java/org/eclipse/edc/sample/extension/policy/TimeIntervalFunction.java)
implements the `AtomicConstraintFunction` interface, which contains a single method for evaluating a constraint.
In that method, the `operator` and `right value` of the constraint can be used for evaluation. In this example, we
want to check whether the evaluation happens within a specified time interval. So depending on whether the operator is
`GT` or `LT`, we check if the current date is before or after the date specified in the `right value`.

```java
@Override
public boolean evaluate(Operator operator, Object rightValue, Permission rule, PolicyContext context) {
    var sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    
    Date date;
    try {
        date = sdf.parse((String) rightValue);
    } catch (ParseException e) {
        monitor.severe("Failed to parse right value of constraint to date.");
        return false;
    }
    
    switch (operator) {
        case LT: var isBefore = new Date().before(date);
            monitor.info("Current date is " + (isBefore ? "before" : "after") + " desired end date.");
            return isBefore;
        case GT: var isAfter = new Date().after(date);
            monitor.info("Current date is " + (isAfter ? "after" : "before") + " desired start date.");
            return isAfter;
        default: return false;
    }
}
```

Next, we have to register our function with the `PolicyEngine` and bind the desired action as well as the key used to
register our function to the desired scopes using the `RuleBindingRegistry`. This is done in the
[`PolicyFunctionsExtension`](policy/policy-01-contract-negotiation/policy-contract-negotiation-policy-functions/src/main/java/org/eclipse/edc/sample/extension/policy/PolicyFunctionsExtension.java):

```java
private final String policyTimeKey = "POLICY_EVALUATION_TIME";

//...

@Override
public void initialize(ServiceExtensionContext context) {
        //...

        ruleBindingRegistry.bind("USE", ALL_SCOPES);
        ruleBindingRegistry.bind(policyTimeKey, ALL_SCOPES);
        policyEngine.registerFunction(ALL_SCOPES, Permission.class, policyTimeKey, new TimeIntervalFunction(monitor));

        //...
}
```

## Defining the policy

Next to registering the function and creating the binding, the `PolicyFunctionsExtension` also creates an asset,
a policy and a contract definition linking the former two. The asset is the same one that is used in
sample [`transfer-01-file-transfer`](transfer/transfer-01-file-transfer/README.md), but this time a different policy is created.

We start by creating the constraints, where we define that the policy evaluation time must be within a certain
time interval. We read the start and end dates for the interval from the settings. Then, we create two constraints:
one specifying that the evaluation time should be after our defined start date, and one specifying that the evaluation
time should be before our defined end date. **We need to set the constraints' left operands to the same key
that we previously used to register our function. Otherwise, the function will not be used to evaluate these
constraints.**

```java
var startDate = context.getSetting(policyStartDateSetting, "2023-01-01T00:00:00.000+02:00");
var notBeforeConstraint = AtomicConstraint.Builder.newInstance()
        .leftExpression(new LiteralExpression(policyTimeKey))
        .operator(Operator.GT)
        .rightExpression(new LiteralExpression(startDate))
        .build();

var endDate = context.getSetting(policyEndDateSetting, "2023-12-31T23:59:00.000+02:00");
var notAfterConstraint = AtomicConstraint.Builder.newInstance()
        .leftExpression(new LiteralExpression(policyTimeKey))
        .operator(Operator.LT)
        .rightExpression(new LiteralExpression(endDate))
        .build();
```

Then, we create a `Permission` with action type `USE` and the two constraints. We use this permission to create and
store a policy. And last, we create the `ContractDefinition`. For the access policy, we use the same use-policy that is
used in sample `transfer:transfer-01-file-transfer`, and for the contract policy, we use the previously created policy with the time
interval restriction. We set the `AssetSelectorExpression` so that the contract definition is valid for our asset.

## How to run the sample

To see the policy enforcement working, this sample should be run twice with different configurations.

### Configuration

Choose one of the following configurations. Depending on which configuration you choose, the contract negotiation
will either be confirmed or declined.

#### 1. Policy fulfilled

Set the start and end date in the [`config.properties`](policy/policy-01-contract-negotiation/policy-contract-negotiation-connector/config.properties) so that the current time is within
the defined interval.

```properties
edc.samples.uc.constraint.date.start=2023-01-01T00:00:00.000+02:00
edc.samples.uc.constraint.date.end=2023-12-31T23:59:00.000+02:00
```

#### 2. Policy not fulfilled

Set the start and end date in the [`config.properties`](policy/policy-01-contract-negotiation/policy-contract-negotiation-connector/config.properties) so that the current time is
**not** within the defined interval.

```properties
edc.samples.policy-01.constraint.date.start=2022-01-01T00:00:00.000+02:00
edc.samples.policy-01.constraint.date.end=2022-12-31T23:59:00.000+02:00
```

### Run the sample

First, build and run the connector for this sample:

```shell
./gradlew policy:policy-01-contract-negotiation:policy-contract-negotiation-connector:build
java -Dedc.fs.config=policy/policy-01-contract-negotiation/policy-contract-negotiation-connector/config.properties -jar policy/policy-01-contract-negotiation/policy-contract-negotiation-connector/build/libs/connector.jar
#for windows:
java -D"edc.fs.config"=policy/policy-01-contract-negotiation/policy-contract-negotiation-connector/config.properties -jar policy/policy-01-contract-negotiation/policy-contract-negotiation-connector/build/libs/connector.jar
```

Next, initiate a contract negotiation. The request body is prepared in [`contractoffer.json`](policy/policy-01-contract-negotiation/contractoffer.json).
In that file, replace the dates for the constraints with the dates you used in the `config.properties` file and then
run:

```shell
curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" -d @samples/uc-workshop/contractoffer.json "http://localhost:8182/api/v1/management/contractnegotiations"
```

Depending on the configuration you chose, you can see the contract negotiation either being confirmed or declined
in the connector logs. The logs also contain messages logged by the `TimeIntervalFunction`, which tell whether the
current date is before or after the start and end dates defined in the policy.
