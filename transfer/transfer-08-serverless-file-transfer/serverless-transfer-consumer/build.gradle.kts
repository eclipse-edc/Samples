plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val groupId: String by project
val edcVersion: String by project

dependencies {
    implementation("$groupId:control-plane-core:$edcVersion")
    implementation("$groupId:http:$edcVersion")
    implementation("$groupId:management-api:$edcVersion")
    implementation("$groupId:api-observability:$edcVersion")
    implementation("$groupId:configuration-filesystem:$edcVersion")
    implementation("$groupId:auth-tokenbased:$edcVersion")
    implementation("$groupId:iam-mock:$edcVersion")
    implementation("$groupId:vault-azure:$edcVersion")
    implementation("$groupId:ids:$edcVersion")
    implementation("$groupId:provision-blob:$edcVersion")
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("consumer.jar")
}

