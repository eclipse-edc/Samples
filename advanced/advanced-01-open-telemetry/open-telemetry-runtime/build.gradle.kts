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
 *       Microsoft Corporation - initial API and implementation
 *       ZF Friedrichshafen AG - add dependency
 *
 */

plugins {
    `java-library`
    application
    alias(libs.plugins.shadow)
}

dependencies {

    runtimeOnly(libs.edc.bom.controlplane.base) {
        exclude(module = "org.eclipse.edc.data-plane-selector-client")
    }
    runtimeOnly(libs.edc.bom.dataplane.base)

    implementation(libs.edc.iam.mock)
    implementation(libs.edc.data.plane.public.api)
    implementation(libs.opentelemetry.exporter.otlp)
    runtimeOnly(libs.edc.monitor.jdk.logger)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("connector.jar")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

val copyOpenTelemetryJar = tasks.register("copyOpenTelemetryJar", Copy::class) {
    val openTelemetry = configurations.create("open-telemetry")

    dependencies {
        openTelemetry(libs.opentelemetry.javaagent)
        openTelemetry(libs.opentelemetry.exporter.otlp)
    }

    from(openTelemetry)
    into("build/otel")
    rename { it -> it.take(it.indexOfLast { it == '-' }) + ".jar"}
}

tasks.shadowJar {
    dependsOn(copyOpenTelemetryJar)
}
