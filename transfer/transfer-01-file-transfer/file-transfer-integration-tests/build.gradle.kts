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
    `java-test-fixtures`
}

val groupId: String by project
val edcVersion: String by project

dependencies {
    testImplementation("$groupId:junit:$edcVersion")

    testFixturesImplementation("$groupId:control-plane-spi:$edcVersion")
    testFixturesImplementation("$groupId:junit:$edcVersion")
    testFixturesImplementation("$groupId:transfer-process-api:$edcVersion")
    testFixturesImplementation("$groupId:api-core:$edcVersion")
    testFixturesImplementation(libs.restAssured)
    testFixturesImplementation(libs.awaitility)
    testFixturesImplementation(libs.assertj)
    testFixturesImplementation(libs.junit.jupiter.api)

    testCompileOnly(project(":transfer:transfer-01-file-transfer:file-transfer-consumer"))
    testCompileOnly(project(":transfer:transfer-01-file-transfer:file-transfer-provider"))
}
