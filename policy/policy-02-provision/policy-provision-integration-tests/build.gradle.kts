/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
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

    testCompileOnly(project(":policy:policy-02-provision:policy-provision-provider"))
    testCompileOnly(project(":policy:policy-02-provision:policy-provision-consumer"))
}