/*
 *  Copyright (c) 2022 Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V. - initial API and implementation
 *
 */


plugins {
    `java-library`
    alias(libs.plugins.edc.build)
}

repositories {
    mavenCentral()
}

val edcVersion = libs.versions.edc

allprojects {
    apply(plugin = "org.eclipse.edc.edc-build")

    configure<org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension> {
        publish.set(false)
    }

    configure<CheckstyleExtension> {
        configFile = rootProject.file("resources/edc-checkstyle-config.xml")
        configDirectory.set(rootProject.file("resources"))
    }

    tasks.test {
        testLogging {
            showStandardStreams = true
        }
    }

}


