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
  web.http.data.port=9192 (default:8181)
  web.http.data.path=/api/v1/management (default:/api)
```
  

## API - Calls

This example shows simple calls related to creating, deleting and retrieving assets, policies and contract definitions. These are not the limitations of the Management API, but it can also be used to retrieve the Federated Catalog, start data transfers, ... . The full capabilities of the Management API are shown in the [OpenAPI document](https://app.swaggerhub.com/apis/eclipse-edc-bot/management-api/0.0.1-SNAPSHOT).

Translated with www.DeepL.com/Translator (free version)

### Basic structur of management API

To contact the management API, the following address applies first:

```
    {EDC-Connector Adress}:{DATAMANAGEMENTPORT}/{MANAGEMENTPATH}
```





### Create

#### Assets

##### API-Call

```http
POST {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/assets
```


Body:


```json
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

```json
{
    "createdAt": timestamp,
    "id": idName
}
```

##### Example:

```
POST http:localhost:9192/api/v1/management/assets
```

Body:

```json
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

```json
{
    "createdAt": 1675079609303,
    "id": "TESTASSETID"
}
```

#### Policies

##### API-Call

```
POST {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/policydefinitions
```

Body:

```json
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

```json
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
```json
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

```json
{
    "createdAt": 1675152359524,
    "id": "TESTPOLICYID"
}
```

#### PolicyDefinitions

##### API-Call

```
POST {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/contractdefinitions
```
Body:

```json
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


```json
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

```json
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

```json
{
    "createdAt": 1675156405307,
    "id": "TESTCONTRACTDEFINITIONID"
}
```


### Delete

##### Call

```
DELETE {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/[ENTITYTYPE]/{ID}
```

[ENTITYTYPE] = assets/policidefinitions/contractdefinitions

Response 204

##### Example:

```
DELETE http:localhost:9192/api/v1/management/assets/TESTASSETID
```



Response 204


#### Get all objects of an entity

##### Call

```
POST {EDC-Connector URL}:{DATAMANAGMENTPORT}/{MANAGEMENTPATH}/[ENTITYTYPE]/request
```

[ENTITYTYPE] = assets/policidefinitions/contractdefinitions

Body:

```json
{}
```


Response 200:

```json
{
  List of all existing objects of the entitytype
}
```

Example:

```
POST http:localhost:9192/api/v1/management/assets/request
```

Body:

```json
{}
```

Response 200:

```json
{
  List of all existing objects of the entitytype
}
```


### More

To learn more about the functionality of the management API, see the [OpenAPI document](https://app.swaggerhub.com/apis/eclipse-edc-bot/management-api/0.0.1-SNAPSHOT) on SwaggerHub.