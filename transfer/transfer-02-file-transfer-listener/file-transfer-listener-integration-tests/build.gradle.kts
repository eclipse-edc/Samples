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
 *       Microsoft Corporation - initial test implementation for sample
 *
 */

plugins {
    `java-library`
}

val groupId: String by project
val edcVersion: String by project

dependencies {
    testImplementation("$groupId:junit:$edcVersion")
    testImplementation(libs.restAssured)
    testImplementation(libs.awaitility)
    testImplementation(testFixtures(project(":transfer:transfer-01-file-transfer:file-transfer-integration-tests")))

    testCompileOnly(project(":transfer:transfer-02-file-transfer-listener:file-transfer-listener-consumer"))
    testCompileOnly(project(":transfer:transfer-01-file-transfer:file-transfer-provider"))
}
