# Running a simple connector

A runnable connector consists of a `Runtime` and a build file, in our case this is a `build.gradle.kts`.

The first thing we need is the `Runtime` which is the main entry point to the connector application, same as with any
other Java program. In this sample we use the
[
`BaseRuntime`](https://github.com/eclipse-edc/Connector/blob/releases/core/common/boot/src/main/java/org/eclipse/edc/boot/system/runtime/BaseRuntime.java),
but this can be extended (take a look at the [`custom-runtime`](../../advanced/advanced-02-custom-runtime) sample for
more information)

The second thing we need is a [gradle build file](build.gradle.kts)
that contains the essential dependencies. We'll need at least the following things:

```kotlin
dependencies {
    implementation(libs.edc.boot)
    implementation(libs.edc.runtime.core)
    implementation(libs.edc.connector.core)
}
```

However, the connector would directly shut down after the successful boot due to no extension is loaded and nothing
happens. To avoid this behavior the [gradle build file](build.gradle.kts) includes already another dependency that we
will re-use in the next samples:

```kotlin
dependencies {
    implementation(libs.edc.http)
}
```

> _Additional dependencies will be added to this list in the future, so be sure to check back regularly!_

With that we can build and run the connector from the root directory:

```bash
./gradlew clean basic:basic-01-basic-connector:build
java -jar basic/basic-01-basic-connector/build/libs/basic-connector.jar
```

_Note: the above snippet assumes that you did not alter the build file, i.e. the `shadow` plugin is used and the build
artifact resides at the path mentioned above. Also, we assume usage of the Gradle Wrapper, as opposed to a local Gradle
installation._

If everything works as intended you should see command-line output similar to this:

```bash
INFO 2025-09-22T09:28:34.533664939 Booting EDC runtime
WARNING 2025-09-22T09:28:34.557375883 The runtime is configured as an anonymous participant. DO NOT DO THIS IN PRODUCTION.
INFO 2025-09-22T09:28:34.6675347 HTTPS enforcement it not enabled, please enable it in a production environment
WARNING 2025-09-22T09:28:34.774747827 Config value: no setting found for 'edc.hostname', falling back to default value 'localhost'
WARNING 2025-09-22T09:28:34.88495366 Using the InMemoryVault is not suitable for production scenarios and should be replaced with an actual Vault!
INFO 2025-09-22T09:28:35.031852619 9 service extensions started
INFO 2025-09-22T09:28:35.032885401 Runtime e1fb90ce-63a0-4d6b-bcdf-8c998b67f41b ready
```

This basic connector - while perfectly fine - does not offer any outward-facing API, nor does it provide any
connector-to-connector communication protocols. However, it will serve us as platform to build out more complex
scenarios.

---

[Next Chapter](../basic-02-health-endpoint/README.md)
