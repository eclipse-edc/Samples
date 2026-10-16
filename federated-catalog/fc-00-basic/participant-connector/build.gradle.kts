/*
 *  Copyright (c) 2025 Cofinity-X
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Cofinity-X - initial API and implementation
 *
 */

plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(libs.edc.bom.controlplane.base) {
        exclude(group = "org.eclipse.edc", "data-plane-signaling-core")
        exclude(group = "org.eclipse.edc", "data-plane-signaling-oauth2")
        exclude(group = "org.eclipse.edc", "data-plane-signaling-oauth2-token-exchange")
    }
    runtimeOnly(libs.edc.iam.mock)
    runtimeOnly(project(":federated-catalog:fc-00-basic:no-op-data-flow"))
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

var distTar = tasks.getByName("distTar")
var distZip = tasks.getByName("distZip")

tasks.shadowJar {
    mergeServiceFiles()
    archiveFileName.set("participant-connector.jar")
    dependsOn(distTar, distZip)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
