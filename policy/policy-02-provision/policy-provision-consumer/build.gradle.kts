plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(libs.edc.control.plane.core)

    implementation(libs.edc.api.observability)

    implementation(libs.edc.configuration.filesystem)
    implementation(libs.edc.iam.mock)

    implementation(libs.edc.auth.tokenbased)
    implementation(libs.edc.management.api)

    implementation(libs.edc.dsp)

    implementation(project(":policy:policy-02-provision:policy-provision-consumer-policy-functions"))
    implementation(project(":policy:policy-02-provision:policy-provision"))

}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("consumer.jar")
}
