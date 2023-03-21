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

include(":transfer:streaming:streaming-01-http-to-http:streaming-01-runtime")
include(":transfer:streaming:streaming-02-kafka-to-http:streaming-02-runtime")
include(":transfer:streaming:streaming-03-kafka-broker:streaming-03-runtime")

include(":advanced:advanced-01-open-telemetry:open-telemetry-consumer")
include(":advanced:advanced-01-open-telemetry:open-telemetry-provider")

//policy
include(":policy:policy-01-contract-negotiation:policy-contract-negotiation-connector")
include(":policy:policy-01-contract-negotiation:policy-contract-negotiation-policy-functions")

// modules for code samples ------------------------------------------------------------------------
include(":other:custom-runtime")
include(":advanced:advanced-02-custom-runtime")

include(":util:http-request-logger")

include(":system-tests")
