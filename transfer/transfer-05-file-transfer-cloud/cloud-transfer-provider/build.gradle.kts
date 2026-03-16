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
 *       Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V. - added dependencies
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

    runtimeOnly(libs.edc.bom.controlplane.base)
    runtimeOnly(libs.edc.iam.mock)
    runtimeOnly(libs.edc.validator.data.address.http.data)
    runtimeOnly(libs.edc.control.plane.api.client)

    runtimeOnly(libs.edc.data.plane.self.registration)
    runtimeOnly(libs.edc.data.plane.signaling.api)
    runtimeOnly(libs.edc.data.plane.core)
    runtimeOnly(libs.edc.data.plane.http)

    runtimeOnly(libs.edc.vault.hashicorp)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.shadowJar {
    mergeServiceFiles()
    archiveFileName.set("provider.jar")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
