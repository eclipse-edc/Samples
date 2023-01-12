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
    `java-test-fixtures`
}

val groupId: String by project
val edcVersion: String by project

dependencies {
    testImplementation("$groupId:junit:$edcVersion")

    testFixturesImplementation("$groupId:junit:$edcVersion")
    testFixturesImplementation(libs.restAssured)
    testFixturesImplementation(libs.awaitility)
    testFixturesImplementation(libs.assertj)
    testFixturesImplementation(libs.junit.jupiter.api)
}