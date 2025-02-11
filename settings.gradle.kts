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

rootProject.name = "samples"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

include(":basic:basic-01-basic-connector")
include(":basic:basic-02-health-endpoint")
include(":basic:basic-03-configuration")

include(":transfer:transfer-00-prerequisites:connector")

include(":transfer:transfer-04-event-consumer:consumer-with-listener")
include(":transfer:transfer-04-event-consumer:listener")

include(":transfer:transfer-05-file-transfer-cloud:cloud-transfer-consumer")
include(":transfer:transfer-05-file-transfer-cloud:cloud-transfer-provider")
include(":transfer:transfer-05-file-transfer-cloud:transfer-file-cloud")

include(":transfer:transfer-06-custom-proxy-data-plane:provider-proxy-data-plane")

include(":transfer:streaming:streaming-01-http-to-http:streaming-01-runtime")
include(":transfer:streaming:streaming-02-kafka-to-http:streaming-02-runtime")
include(":transfer:streaming:streaming-03-kafka-broker:streaming-03-runtime")

include(":advanced:advanced-01-open-telemetry:open-telemetry-runtime")

//policy
include(":policy:policy-01-policy-enforcement:policy-enforcement-provider")
include(":policy:policy-01-policy-enforcement:policy-enforcement-consumer")
include(":policy:policy-01-policy-enforcement:policy-functions")

// federated catalog
include(":federated-catalog:fc-00-basic:federated-catalog-base")
include(":federated-catalog:fc-00-basic:fixed-node-resolver")
include(":federated-catalog:fc-01-embedded:fc-connector")
include(":federated-catalog:fc-02-standalone:standalone-fc")
include(":federated-catalog:fc-03-static-node-directory:target-node-resolver")
include(":federated-catalog:fc-03-static-node-directory:standalone-fc-with-node-resolver")
include(":federated-catalog:fc-03-static-node-directory:embedded-fc-with-node-resolver")


// modules for code samples ------------------------------------------------------------------------
include(":advanced:advanced-02-custom-runtime")

include(":util:http-request-logger")

include(":system-tests")
