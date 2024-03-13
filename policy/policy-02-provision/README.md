# Create a policy for provisioning 

Now that we know how to transfer a file between two connectors and how to write policies, in this step we will see how we 
can use policies to modify the supporting data transfer infrastructure. We will be regulating the file destination in a file transfer process. We will use a policy
defined in [`provision.menifest.verify`](https://eclipse-edc.github.io/docs/#/submodule/Connector/docs/developer/policy-engine?id=manifest-verification-scope-provisionmanifestverify)
scope. This scope is used during the provisioning phase to evaluate the resource definitions of a generated resource manifest.
Policy functions registered in this scope may modify resource definitions so that they comply with the policy.

## Defining the policy for provider

First we will create a policy definition for the provider which contains `regulateFilePathConstraint`. We can define this policy the same way we did it for the sample [`policy-01-contract-negotiation`](policy/policy-01-contract-negotiation). 
The contract policy is implemented the following way:

```java
// in PolicyFunctionsExtension.java
var desiredFilePath = context.getSetting(policyRegulatedFilePathSetting, "/tmp/provider/test-document.txt");
var regulateFilePathConstraint = AtomicConstraint.Builder.newInstance()
        .leftExpression(new LiteralExpression(policyRegulateFilePath))
        .operator(Operator.EQ)
        .rightExpression(new LiteralExpression(desiredFilePath))
        .build();
    
var permission = Permission.Builder.newInstance()
        .action(Action.Builder.newInstance().type("USE").build())
        .constraint(regulateFilePathConstraint)
        .build();

return PolicyDefinition.Builder.newInstance()
        .id("use-regulated-path")
        .policy(Policy.Builder.newInstance()
        .permission(permission)
        .build())
        .build();
```
We do not need to register any function for this policy. This is because in our current sample the `regulateFilePathConstraint` 
will be applied on consumer’s `ResourceDefinition`. So we will be registering related functions in consumer’s policy functions. 
We will see that in a moment.


## Modifying file transfer json
In previous file transfer example, while requesting a file, in [`transfer-01-file-transfer/filetransfer.json`](transfer/transfer-01-file-transfer/filetransfer.json) 
we mentioned the file destination path, and set `managedResources` to `false`. But for this sample, we will set the `managedResources` to `true` and will not be 
defining any file destination path. The request body for file transfer has been defined in [`filetransfer.json`](policy/policy-02-provision/filetransfer.json).

```json
{
  ...
  "dataDestination": {
    "properties": {
      "type": "File"
    }
  },
  ...
  "managedResources": true,
  ...
}
```
This will allow the `ResourceManifestGenerator` to generate a `ResourceManifest`, from a `ResourceDefinition` 
according to our required file type mentioned in [`filetransfer.json`](policy/policy-02-provision/filetransfer.json). 


## Defining provisioner and resource definition generator

For simplicity, we are doing a local file transfer, and have implemented necessary codes required for related
resource definition generator and provisioner in the module [`policy-provision`](policy/policy-02-provision/policy-provision). 
[`LocalConsumerResourceDefinitionGenerator`](policy/policy-02-provision/policy-provision/src/main/java/org/eclipse/sample/extension/provision/LocalConsumerResourceDefinitionGenerator.java)
implements `ConsumerResourceDefinitionGenerator` which generates `ResourceDefinition` for our required file type `File`.

```java
// in LocalConsumerResourceDefinitionGenerator.java

private static final String TYPE = "File";

// this path will get modified during the policy evaluation to notice the change, keep the path different from the path used in policy 
private static final String DESTINATION = "any path"; 

@Override
public @Nullable ResourceDefinition generate(DataRequest dataRequest, Policy policy) {
    Objects.requireNonNull(dataRequest, "dataRequest must always be provided");
    Objects.requireNonNull(policy, "policy must always be provided");

    var destination = DESTINATION;
    var id = randomUUID().toString();

    return LocalResourceDefinition.Builder.newInstance()
        .id(id)
        .pathName(destination)
        .build();
}
@Override
public boolean canGenerate(DataRequest dataRequest, Policy policy) {
    Objects.requireNonNull(dataRequest, "dataRequest must always be provided");
    Objects.requireNonNull(policy, "policy must always be provided");

    return TYPE.equals(dataRequest.getDestinationType());
}
```

[`LocalProvisionExtension`](policy/policy-02-provision/policy-provision/src/main/java/org/eclipse/sample/extension/provision/LocalProvisionExtension.java)
generates a [`LocalResourceProvisioner`](policy/policy-02-provision/policy-provision/src/main/java/org/eclipse/sample/extension/provision/LocalResourceProvisioner.java)
which is our required type of provisioner for local resources.


## Creating and registering the policy function for consumer

Now, as we are willing to modify the data destination according to our policy, we have to define a policy that will  
be evaluated in [`provision.menifest.verify`](https://eclipse-edc.github.io/docs/#/submodule/Connector/docs/developer/policy-engine?id=manifest-verification-scope-provisionmanifestverify)
scope.

As the data destination address is defined in consumer ResourceManifest, we have to write a policy function that will 
be used by consumer connector. [`policy-provision-consumer-policy-functions`](policy/policy-02-provision/policy-provision-consumer-policy-functions) 
module includes the policy functions for consumer connector. 


The [`RegulateFilePathFunction `](policy/policy-02-provision/policy-provision-consumer-policy-functions/src/main/java/org/eclipse/sample/extension/provision/consumer/policy/RegulateFilePathFunction.java)
implements the `AtomicConstraintFunction` interface, which contains a single method for evaluating a constraint.
In that method, the `operator` `EQ` and desired `pathname` in the `right value` of the constraint are used for evaluation. 
In this example, we updated the `pathName` in `LocalResourceDefinition` to our desired `pathName` which was defined in our policy.


Next, we have to register our function with the `PolicyEngine` and bind the desired action as well as the key used to
register our function to the desired scopes using the `RuleBindingRegistry`. This is done in the
[`ConsumerPolicyFunctionsExtension`](policy/policy-02-provision/policy-provision-consumer-policy-functions/src/main/java/org/eclipse/sample/extension/provision/consumer/policy/ConsumerPolicyFunctionsExtension.java):

```java
private final String policyRegulateFilePath = "POLICY_REGULATE_FILE_PATH";

//...

@Override
public void initialize(ServiceExtensionContext context) {
    //...

    ruleBindingRegistry.bind("USE", ALL_SCOPES);
    ruleBindingRegistry.bind(policyRegulateFilePath, MANIFEST_VERIFICATION_SCOPE);
    policyEngine.registerFunction(MANIFEST_VERIFICATION_SCOPE, Permission.class, policyRegulateFilePath, new RegulateFilePathFunction(monitor));

    //...
}
```

Here, we do not need to define any policy, as this policy function will be used by consumer connector.


## How to run the sample

Running this sample consists of the same steps done in file transfer sample.

### Configuration

Set the desired path address in the provider [`config.properties`](policy/policy-02-provision/policy-provision-provider/config.properties).

```properties
edc.samples.policy-02.constraint.desired.file.path = path/to/desired/location/transfer.txt
```

### Run the sample

### 1. Build and start the connectors
First, build and run the provider and consumer connector for this sample:

Build and run the consumer connector:
```shell
./gradlew policy:policy-02-provision:policy-provision-consumer:build

java -Dedc.fs.config=policy/policy-02-provision/policy-provision-consumer/config.properties -jar policy/policy-02-provision/policy-provision-consumer/build/libs/consumer.jar
# for windows
java -D"edc.fs.config"=policy/policy-02-provision/policy-provision-consumer/config.properties -jar policy/policy-02-provision/policy-provision-consumer/build/libs/consumer.jar
```
In another terminal, build and run the provider connector:
```shell
./gradlew policy:policy-02-provision:policy-provision-provider:build

java -Dedc.fs.config=policy/policy-02-provision/policy-provision-provider/config.properties -jar policy/policy-02-provision/policy-provision-provider/build/libs/provider.jar
# for windows
java -D"edc.fs.config"=policy/policy-02-provision/policy-provision-provider/config.properties -jar policy/policy-02-provision/policy-provision-provider/build/libs/provider.jar
```

### 2. Initiate a contract negotiation
Next, initiate a contract negotiation. The request body is prepared in [`contractoffer.json`](policy/policy-02-provision/contractoffer.json).
Then run:

```shell
curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" -d @policy/policy-02-provision/contractoffer.json "http://localhost:9192/management/v2/contractnegotiations"
```

### 3. Look up the contract agreement ID

Look up the contract agreement ID:

```bash
curl -X GET -H 'X-Api-Key: password' "http://localhost:9192/management/v2/contractnegotiations/<UUID>"
```

### 4. Request the file

Request file transfer with the request body [`filetransfer.json`](policy/policy-02-provision/filetransfer.json):

```bash
curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" -d @policy/policy-02-provision/filetransfer.json "http://localhost:9192/management/v2/transferprocesses" 
```

### 5. See transferred file

After the file transfer is completed, we can check the destination path specified in the policy/[`config.properties`](policy/policy-02-provision/policy-provision-provider/config.properties)
for the file. Here, we'll now find a file with the same content as the original file offered by the provider. We should notice that even though 
`LocalConsumerResourceDefinitionGenerator` defined a different destination for the file, the path is getting modified according to the 
policy.

---