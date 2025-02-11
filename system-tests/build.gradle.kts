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
 *       Fraunhofer-Gesellschaft - dependencies for Federated Catalog Tests
 *       Fraunhofer-Gesellschaft - set working directory to project directory
 *
 */

plugins {
    `java-library`
}

dependencies {
    testImplementation(libs.edc.junit)
    testImplementation(libs.edc.json.ld.lib)
    testImplementation(libs.edc.json.ld.spi)
    testImplementation(libs.edc.control.plane.spi)
    testImplementation(testFixtures(libs.edc.management.api.test.fixtures))
    testImplementation(libs.awaitility)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.restAssured)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.kafka.clients)
    testImplementation(libs.testcontainers.minio)
    testImplementation(libs.testcontainers.hashicorp.vault)
    testImplementation(libs.azure.storage.blob)
    testImplementation(libs.minio.io)

    // runtimes
    testCompileOnly(project(":basic:basic-01-basic-connector"))
    testCompileOnly(project(":basic:basic-02-health-endpoint"))
    testCompileOnly(project(":basic:basic-03-configuration"))

    testCompileOnly(project(":transfer:transfer-00-prerequisites:connector"))
    testCompileOnly(project(":transfer:transfer-04-event-consumer:consumer-with-listener"))
    testCompileOnly(project(":transfer:transfer-04-event-consumer:listener"))
    testCompileOnly(project(":transfer:streaming:streaming-01-http-to-http:streaming-01-runtime"))
    testCompileOnly(project(":transfer:streaming:streaming-02-kafka-to-http:streaming-02-runtime"))
    testCompileOnly(project(":transfer:streaming:streaming-03-kafka-broker:streaming-03-runtime"))

    testCompileOnly(project(":advanced:advanced-01-open-telemetry:open-telemetry-runtime"))

    testCompileOnly(project(":policy:policy-01-policy-enforcement:policy-enforcement-provider"))
    testCompileOnly(project(":policy:policy-01-policy-enforcement:policy-enforcement-consumer"))
    testCompileOnly(project(":policy:policy-01-policy-enforcement:policy-functions"))

    testCompileOnly(project(":transfer:transfer-05-file-transfer-cloud:cloud-transfer-provider"))
    testCompileOnly(project(":transfer:transfer-05-file-transfer-cloud:cloud-transfer-consumer"))
    testCompileOnly(project(":transfer:transfer-05-file-transfer-cloud:transfer-file-cloud"))

    testCompileOnly(project(":transfer:transfer-06-custom-proxy-data-plane:provider-proxy-data-plane"))

    testCompileOnly(project(":federated-catalog:fc-00-basic:fixed-node-resolver"))
    testCompileOnly(project(":federated-catalog:fc-01-embedded:fc-connector"))
    testCompileOnly(project(":federated-catalog:fc-02-standalone:standalone-fc"))
    testCompileOnly(project(":federated-catalog:fc-03-static-node-directory:target-node-resolver"))
    testCompileOnly(project(":federated-catalog:fc-03-static-node-directory:standalone-fc-with-node-resolver"))
    testCompileOnly(project(":federated-catalog:fc-03-static-node-directory:embedded-fc-with-node-resolver"))
}

tasks.compileJava {
    dependsOn(":advanced:advanced-01-open-telemetry:open-telemetry-runtime:build")
}
