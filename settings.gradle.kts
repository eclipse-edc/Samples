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

// this is needed to have access to snapshot builds of plugins
pluginManagement {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        mavenCentral()
        mavenLocal()
    }
    versionCatalogs {
        create("libs") {
            from("org.eclipse.edc:edc-versions:0.0.1-milestone-8")
            // this is not part of the published EDC Version Catalog, so we'll just "amend" it
            library(
                    "dnsOverHttps",
                    "com.squareup.okhttp3",
                    "okhttp-dnsoverhttps"
            ).versionRef("okhttp")
        }
    }
}

// basic
include(":basic:basic-01-basic-connector")
include(":basic:basic-02-health-endpoint")
include(":basic:basic-03-configuration")

// transfer
include(":transfer:transfer-01-file-transfer:file-transfer-consumer")
include(":transfer:transfer-01-file-transfer:file-transfer-provider")
include(":transfer:transfer-01-file-transfer:file-transfer-integration-tests")
include(":transfer:transfer-01-file-transfer:transfer-file-local")
include(":transfer:transfer-01-file-transfer:status-checker")

include(":transfer:transfer-02-file-transfer-listener:file-transfer-listener-consumer")
include(":transfer:transfer-02-file-transfer-listener:file-transfer-listener-integration-tests")
include(":transfer:transfer-02-file-transfer-listener:listener")

include(":transfer:transfer-03-modify-transferprocess:api")
include(":transfer:transfer-03-modify-transferprocess:modify-transferprocess-consumer")
include(":transfer:transfer-03-modify-transferprocess:modify-transferprocess-integration-tests")
include(":transfer:transfer-03-modify-transferprocess:simulator")
include(":transfer:transfer-03-modify-transferprocess:watchdog")

include(":transfer:transfer-04-open-telemetry:open-telemetry-consumer")
include(":transfer:transfer-04-open-telemetry:open-telemetry-provider")

include(":transfer:transfer-05-file-transfer-cloud:cloud-transfer-consumer")
include(":transfer:transfer-05-file-transfer-cloud:cloud-transfer-provider")
include(":transfer:transfer-05-file-transfer-cloud:transfer-file-cloud")

include("transfer:transfer-06-consumer-pull-http:http-pull-connector")
include("transfer:transfer-06-consumer-pull-http:consumer-pull-backend-service")

include("transfer:transfer-07-provider-push-http:http-push-connector")
include("transfer:transfer-07-provider-push-http:provider-push-http-backend-service")

include(":transfer:transfer-08-serverless-file-transfer:serverless-transfer-consumer")
include(":transfer:transfer-08-serverless-file-transfer:serverless-transfer-provider")
include(":transfer:transfer-08-serverless-file-transfer:serverless-transfer-file")
// modules for code samples ------------------------------------------------------------------------
include(":other:custom-runtime")
