/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Fraunhofer Institute for Software and Systems Engineering - added dependencies
 *
 */

plugins {
    `java-library`
    id("application")
}

dependencies {
    api(libs.edc.control.plane.spi)
    implementation(libs.edc.data.plane.core)
    implementation(libs.edc.data.plane.util)
    implementation(libs.edc.data.plane.client)
    implementation(libs.edc.data.plane.selector.client)
    implementation(libs.edc.data.plane.selector.core)
    implementation(libs.edc.transfer.data.plane)
    implementation(libs.opentelemetry.annotations)
}