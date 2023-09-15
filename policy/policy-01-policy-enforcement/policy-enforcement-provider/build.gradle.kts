plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}


dependencies {
    implementation(libs.edc.control.plane.core)

    implementation(libs.edc.configuration.filesystem)
    implementation(libs.edc.iam.mock)

    implementation(libs.edc.dsp)

    implementation(project(":policy:policy-01-policy-enforcement:policy-functions"))
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("provider.jar")
}
