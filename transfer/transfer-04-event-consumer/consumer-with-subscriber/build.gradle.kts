/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(libs.edc.bom.controlplane.base)
    implementation(libs.edc.control.plane.api.client)
    runtimeOnly(libs.edc.participant.context.single.core)
    runtimeOnly(libs.edc.iam.mock)
    runtimeOnly(libs.edc.transfer.data.plane.signaling)
    runtimeOnly(libs.edc.validator.data.address.http.data)

    runtimeOnly(libs.edc.edr.cache.api)
    runtimeOnly(libs.edc.edr.store.core)
    runtimeOnly(libs.edc.edr.store.receiver)

    runtimeOnly(libs.edc.data.plane.self.registration)
    runtimeOnly(libs.edc.data.plane.signaling.api)
    runtimeOnly(libs.edc.data.plane.core)
    runtimeOnly(libs.edc.data.plane.http)
    runtimeOnly(libs.edc.data.plane.iam)

    implementation(libs.edc.control.plane.spi)
}

application {
    mainClass.set("$group.boot.system.runtime.BaseRuntime")
}

var distTar = tasks.getByName("distTar")
var distZip = tasks.getByName("distZip")

tasks.shadowJar {
    mergeServiceFiles()
    archiveFileName.set("connector.jar")
    dependsOn(distTar, distZip)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
