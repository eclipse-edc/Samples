/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Fraunhofer Institute for Software and Systems Engineering - added dependencies
 *       ZF Friedrichshafen AG - add dependency
 *
 */

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val groupId: String by project
val edcVersion: String by project

dependencies {
    implementation("$groupId:control-plane-core:$edcVersion")

    implementation("$groupId:api-observability:$edcVersion")

    implementation("$groupId:configuration-filesystem:$edcVersion")
    implementation("$groupId:iam-mock:$edcVersion")
    implementation("$groupId:vault-azure:$edcVersion")
    implementation("$groupId:http:$edcVersion")

    implementation("$groupId:auth-tokenbased:$edcVersion")
    implementation("$groupId:management-api:$edcVersion")

    implementation("$groupId:ids:$edcVersion")

    implementation(project(":transfer:transfer-05-file-transfer-cloud:transfer-file-cloud"))
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("provider.jar")
}
