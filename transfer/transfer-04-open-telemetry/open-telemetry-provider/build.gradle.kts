/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial implementation
 *
 */

plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(libs.edc.control.plane.core)
    implementation(libs.edc.data.plane.selector.core)

    implementation(libs.edc.api.observability)

    implementation(libs.edc.configuration.filesystem)
    implementation(libs.edc.iam.mock)

    implementation(libs.edc.auth.tokenbased)
    implementation(libs.edc.management.api)

    implementation(libs.edc.dsp)

    implementation(project(":transfer:transfer-01-file-transfer:transfer-file-local"))
    runtimeOnly(libs.edc.monitor.jdk.logger)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("provider.jar")
}

tasks.register("downloadOpenTelemetryJar"){
    val filePath = "../opentelemetry-javaagent.jar"
    val file = File(filePath)
    if (!file.isFile) {
        val sourceUrl = "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.27.0/opentelemetry-javaagent.jar"
        download(sourceUrl,filePath)
    }
}

tasks.build {
    dependsOn("downloadOpenTelemetryJar")
}

fun download(url : String, path : String){
    val destFile = File(path)
    ant.invokeMethod("get", mapOf("src" to url, "dest" to destFile))
}