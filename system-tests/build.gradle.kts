/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
}

dependencies {
    implementation(libs.edc.transfer.spi)

    testImplementation(libs.edc.junit)
    testImplementation(libs.edc.json.ld)
    testImplementation(libs.awaitility)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.restAssured)

    // runtimes
    testCompileOnly(project(":basic:basic-01-basic-connector"))
    testCompileOnly(project(":basic:basic-02-health-endpoint"))
    testCompileOnly(project(":basic:basic-03-configuration"))
    testCompileOnly(project(":transfer:transfer-01-file-transfer:file-transfer-consumer"))
    testCompileOnly(project(":transfer:transfer-01-file-transfer:file-transfer-provider"))
    testCompileOnly(project(":transfer:transfer-02-file-transfer-listener:file-transfer-listener-consumer"))
    testCompileOnly(project(":transfer:transfer-03-modify-transferprocess:modify-transferprocess-consumer"))
    testCompileOnly(project(":transfer:streaming:streaming-01-http-to-http:streaming-01-runtime"))
}
