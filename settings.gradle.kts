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
            from("org.eclipse.edc:edc-versions:0.0.1-SNAPSHOT")
            // this is not part of the published EDC Version Catalog, so we'll just "amend" it
            library("dnsOverHttps", "com.squareup.okhttp3", "okhttp-dnsoverhttps").versionRef("okhttp")
        }
    }
}

include(":01-basic-connector")
include(":02-health-endpoint")
include(":03-configuration")

include(":04.0-file-transfer:file-transfer-consumer")
include(":04.0-file-transfer:file-transfer-provider")
include(":04.0-file-transfer:file-transfer-integration-tests")
include(":04.0-file-transfer:transfer-file-local")
include(":04.0-file-transfer:status-checker")

include(":04.1-file-transfer-listener:file-transfer-listener-consumer")
include(":04.1-file-transfer-listener:file-transfer-listener-integration-tests")
include(":04.1-file-transfer-listener:listener")

include(":04.2-modify-transferprocess:api")
include(":04.2-modify-transferprocess:modify-transferprocess-consumer")
include(":04.2-modify-transferprocess:modify-transferprocess-integration-tests")
include(":04.2-modify-transferprocess:simulator")
include(":04.2-modify-transferprocess:watchdog")

include(":04.3-open-telemetry:open-telemetry-consumer")
include(":04.3-open-telemetry:open-telemetry-provider")

include(":05-file-transfer-cloud:cloud-transfer-consumer")
include(":05-file-transfer-cloud:cloud-transfer-provider")
include(":05-file-transfer-cloud:transfer-file-cloud")

// modules for code samples ------------------------------------------------------------------------
include(":other:custom-runtime")