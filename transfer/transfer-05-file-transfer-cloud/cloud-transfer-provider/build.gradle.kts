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
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":transfer:transfer-05-file-transfer-cloud:transfer-file-cloud"))

    implementation(libs.edc.runtime.core)
    implementation(libs.edc.connector.core)
    implementation(libs.edc.control.api.configuration)
    implementation(libs.edc.control.plane.api.client)
    implementation(libs.edc.control.plane.api)
    implementation(libs.edc.control.plane.core)
    implementation(libs.edc.dsp)
    implementation(libs.edc.http)
    implementation(libs.edc.configuration.filesystem)
    implementation(libs.edc.iam.mock)
    implementation(libs.edc.management.api)
    implementation(libs.edc.edr.store.core)
    implementation(libs.edc.transfer.data.plane.signaling)
    implementation(libs.edc.validator.data.address.http.data)

    implementation(libs.edc.data.plane.selector.api)
    implementation(libs.edc.data.plane.selector.core)

    implementation(libs.edc.data.plane.self.registration)
    implementation(libs.edc.data.plane.signaling.api)
    implementation(libs.edc.data.plane.public.api)
    implementation(libs.edc.data.plane.core)
    implementation(libs.edc.data.plane.http)

    implementation(libs.edc.vault.hashicorp)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("provider.jar")
}
