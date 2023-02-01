# Use the management API

The management API is the interface between the EDC and the user/software to interact with it. With it, for example, new assets, policies and policy definitions can be created, deleted or queried. But also other points such as querying the catalog of an EDC connector or triggering a transfer process is done via the management API.

## Needed Dependency
```kotlin
dependencies {
    // ...
    implementation("$groupId:data-management-api:$edcVersion")
    // ...
}
```

## Configuartion

The management API has two configurable parameters, which can be defined as shown in example [basic-03-configuration](../basic-03-configuration/README.md).

Example:
```
  web.http.data.port=9192
  web.http.data.path=/api/v1/management
```
  

## API - Calls

This example shows simple calls related to creating, deleting and retrieving assets, policies and contract definitions. These are not the limitations of the Management API, but it can also be used to retrieve the Federated Catalog, start data transfers, ... . The full capabilities of the Management API are shown in the [OpenAPI document](https://app.swaggerhub.com/apis/eclipse-edc-bot/management-api/0.0.1-SNAPSHOT).

Translated with www.DeepL.com/Translator (free version)

### Basic structur of management API

To contact the management API, the following address applies first:

```
    {EDC-Connector Adress}:{DATAMANAGEMENTPORT}/{MANAGEMENTPATH}
```



### Assets

#### Create an asset

##### API-Call

```
POST {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/assets
```


Body:


```
{
  "asset": {
        "properties": {
            "asset:prop:id": {ASSETID},
            ...
        }
    },
    "dataAddress": { 
        "properties": {
            "type": {DATAADRESSTYPE},
            ...
        }
  }
}
```


Response 200

```
{
    "createdAt": timestamp,
    "id": idName
}
```

##### Example:

```
POST http:localhost:9192/api/v1/management/assets
```

```
{
    "asset": {
        "properties": {
            "asset:prop:id": "TESTASSETID",
            "asset:prop:description": "This is an Test asset",
            "asset:prop:version": "1.0",
            "asset:prop:contenttype": "text/plain"
        }
    },
    "dataAddress": { 
        "properties": {
            "type": "HttpData",
            "baseUrl": "{Link to an HTTP-GetSource}",
            "name": "{Specific Endpoint}"
        }
    }
}
```


Response 200

```
{
    "createdAt": 1675079609303,
    "id": "TESTASSETID"
}
```



#### Delete an Asset

##### Call

```
DELETE {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/assets/{ASSETID}
```

Response 204

##### Example:

```
DELETE http:localhost:9192/api/v1/management/assets/TESTASSETID
```



Response 204


#### Get all Assets

##### Call

```
POST {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/assets/request
```

Body:

```
{}
```


Response 200:

```
{
  List of all created Assets
}
```

Example:

```
POST http:localhost:9192/api/v1/management/assets/request
```

Body:

```
{}
```

Response 200:

```
{
  List of all created Assets
}
```

### Policies

#### Create a Policy

##### API-Call

```
POST {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/policydefinitions
```

Body:

```
{
    "id": {POLICYID},
    "policy": {
        "permissions": [
            {
                "action": {
                    "type": "USE"
                },
                "edctype": "dataspaceconnector:permission",
                ...
            }
        ]
    }
}
```

Response 200:

```
{
    "createdAt": timestamp,
    "id": idName
}
```

##### Example:

```
POST http:localhost:9192/api/v1/management/policydefinitions
```

Body:
```
{
    "id": "TESTPOLICYID",
    "policy": {
        "permissions": [
            {
                "action": {
                    "type": "USE"
                },
                "edctype": "dataspaceconnector:permission"
            }
        ]
    }
}
```

Response 200:

```
{
    "createdAt": 1675152359524,
    "id": "TESTPOLICYID"
}
```


#### Delete a policy
##### API-Call

```
DELETE {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/policydefinitions/{POLICYID}
```

Response 204

##### Example:

```
DELETE http:localhost:9192/api/v1/management/policydefinitions/TESTPOLICYID
```

Response 204

#### GET all Policies

##### API-Call

```
POST {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/policydefinitions/request
```

Body:

```
{}
```

Response 200:

```
{
  List of all created Policies
}
```

##### Example:

```
POST http:localhost:9192/api/v1/management/policydefinitions/request
```

Body:

```
{}
```

Response 200:

```
{
  List of all created Policies
}
```

### Policydefinitions

#### Create a policy

##### API-Call

```
POST {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/contractdefinitions
```
Body:

```
{
    "accessPolicyId": {ACCESSPOLICYID},
    "contractPolicyId": {CONTRACTPOLICYID},
    "criteria": [
        {
            "operandLeft": "asset:prop:id",
            "operator": "=",
            "operandRight": {ASSETID}
        }
    ],
    "id": {CONTRACTDEFINITIONID}
}
```

Response 200:


```
{
    "createdAt": {TIMESTAMP},
    "id": {CONTRACTDEFINITIONID}
}
```

##### Example:


```
POST http:localhost:9192/api/v1/management/contractdefinitions
```

Body:

```
{
    "accessPolicyId": "TESTPOLICYID",
    "contractPolicyId": "TESTPOLICYID",
    "criteria": [
        {
            "operandLeft": "asset:prop:id",
            "operator": "=",
            "operandRight": "TESTASSETID"
        }
    ],
    "id": "TESTCONTRACTDEFINITIONID"
}
```

Response 200:

```
{
    "createdAt": 1675156405307,
    "id": "TESTCONTRACTDEFINITIONID"
}
```


#### Delete a policy

##### API-Call

```
DELETE {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/contractdefinitions/{CONTRACTDEFINITIONID}
```

Response 204

##### Example:

```
DELETE http:localhost:9192/api/v1/management/contractdefinitions/TESTCONTRACTDEFINITIONID
```

#### Get all policies

##### API-Call

```
POST {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/contractdefinitions/request
```

Body:

```
{}
```

Response 200:

```
List of all Contractdefinitions
```


##### Example:


```
POST http:localhost:9192/api/v1/management/contractdefinitions/request
```

Body:

```
{}
```

Response 200:

```
List of all Contractdefinitions
```

### More

To learn more about the functionality of the management API, see the [OpenAPI document](https://app.swaggerhub.com/apis/eclipse-edc-bot/management-api/0.0.1-SNAPSHOT) on SwaggerHub.