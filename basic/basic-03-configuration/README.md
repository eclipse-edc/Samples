# Use the filesystem-based configuration

So far we have not had any way to configure our system and use the default hard-coded values, which generally is not an
elegant way.

The Eclipse Dataspace Connector exposes configuration through its `ConfigurationExtension` interface. That is a ["
special" extension](https://github.com/eclipse-edc/Connector/blob/main/spi/common/boot-spi/src/main/java/org/eclipse/edc/spi/system/ConfigurationExtension.java) in that sense that it gets loaded at a very early stage. There is also a default implementation
named [`FsConfigurationExtension.java`](https://github.com/eclipse-edc/Connector/blob/main/extensions/common/configuration/configuration-filesystem/src/main/java/org/eclipse/edc/configuration/filesystem/FsConfigurationExtension.java)
which uses a standard Java properties file to store configuration entries.

In the previous steps we had not included that in the JAR file, so we need to add
the `configuration-filesystem` module to the dependency list:

```kotlin
dependencies {
    // ...
    implementation(libs.edc.configuration.filesystem)
    // ...
}
```

We compile and run the application with:

```bash
./gradlew clean basic:basic-03-configuration:build
java -jar basic/basic-03-configuration/build/libs/filesystem-config-connector.jar --log-level=DEBUG
```

you will notice an additional log line stating that the "configuration file does not exist":

```bash
INFO 2021-09-07T08:26:08.282159 Configuration file does not exist: dataspaceconnector-configuration.properties. Ignoring.
```

## Set up the configuration extension

By default, the `FsConfigurationExtension` expects there to be a properties file
named `dataspaceconnector-configuration.properties` located in the current directory. The name (and path) of the config
file is configurable using the `edc.fs.config` property, so we can customize this to our liking.

First, create a properties file in a location of your convenience,
e.g. `/etc/eclipse/dataspaceconnector/config.properties`.

```bash
mkdir -p /etc/eclipse/dataspaceconnector
touch /etc/eclipse/dataspaceconnector/config.properties
```

Second, lets reconfigure the Jetty Web Server to listen to port `9191` instead of the default `8181`. Open
the `config.properties` with a text editor of your choice and add the following line:

```properties
web.http.port=9191
```

An example file can be found [here](config.properties). Clean, rebuild and run the connector again, but this time
passing the path to the config file:

```bash
java -Dedc.fs.config=/etc/eclipse/dataspaceconnector/config.properties -jar basic/basic-03-configuration/build/libs/filesystem-config-connector.jar --log-level=DEBUG
```

Observing the log output we now see that 

```bash
INFO 2021-09-07T08:26:08.282159 ConfigurationExtension Initialized: FS Configuration
```
 and that the connector's REST API is exposed on port `9191` instead:

```bash
DEBUG 2025-07-28T12:39:14.710420377 HTTP context 'default' listening on port 9191
DEBUG 2025-07-28T12:39:14.770991316 Port mappings: PortMapping[name=default, port=9191, path=/api]
DEBUG 2025-07-28T12:39:14.771240769 Started Jetty Service
```

## Add your own configuration value

Let's say we want to have a configurable log prefix in our health REST endpoint. The way to do this involves two steps:

1. add the config value to the `config.properties` file
2. access and read the config value from code

### 1. Add the config value

Simply add a new line with an arbitrary key to your `config.properties`:

```properties
edc.samples.basic.03.logprefix=MyLogPrefix
```

### 2. Access the config value

The `ServiceExtensionContext` exposes a method `getSettings(String, Object)` to read settings (i.e. config values)'.
Modify the code from the `HealthEndpointExtension.java` as shown below (use the one from the `03-configuration`
of course):

```java
public class HealthEndpointExtension implements ServiceExtension {

    @Inject
    WebService webService;

    private static final String LOG_PREFIX_SETTING = "edc.samples.basic.03.logprefix"; // this constant is new

    @Override
    public void initialize(ServiceExtensionContext context) {
        var logPrefix = context.getSetting(LOG_PREFIX_SETTING, "health"); //this line is new
        webService.registerResource(new HealthApiController(context.getMonitor(), logPrefix));
    }
}
```

Next, we must modify the constructor signature of the `HealthApiController` class and store the `logPrefix` as variable:

```java

@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
@Path("/")
public class HealthApiController {

    private final Monitor monitor;
    private final String logPrefix;

    public HealthApiController(Monitor monitor, String logPrefix) {
        this.monitor = monitor;
        this.logPrefix = logPrefix;
    }

    @GET
    @Path("health")
    public String checkHealth() {
        monitor.info(String.format("%s :: Received a health request", logPrefix));
        return "{\"response\":\"I'm alive!\"}";
    }
}
```

There are a few things worth mentioning here:

- things like configuration value names should be implemented as constants, e.g. `LOG_PREFIX_SETTING` and should have a
  consistent and hierarchical naming scheme
- if a config value is not present, we should either specify a default value (i.e. `"health"`) or throw
  an `EdcException`
- configuration values should be handled in the `*Extension` class, as it's job is to set up the extension and its
  required business logic (e.g. the controller). The extension itself should not contain any business logic
- it's better to pass the config value directly into the business logic than passing the
  entire `ServiceExtensionContext`, using configuration objects when there are more than one

[Previous Chapter](../basic-02-health-endpoint/README.md) 
