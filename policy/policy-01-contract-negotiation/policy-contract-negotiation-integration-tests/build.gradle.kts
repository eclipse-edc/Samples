/*
 *  Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
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

    testFixturesImplementation("$groupId:junit:$edcVersion")
    testFixturesImplementation("io.rest-assured:rest-assured:5.3.0")
    testFixturesImplementation("org.awaitility", "awaitility", "4.2.0")
    testFixturesImplementation("org.assertj", "assertj-core", "3.24.2")
    testFixturesImplementation("org.junit.jupiter", "junit-jupiter-api", "5.9.2")

    testCompileOnly(project(":policy:policy-01-contract-negotiation:policy-contract-negotiation-connector"))
}