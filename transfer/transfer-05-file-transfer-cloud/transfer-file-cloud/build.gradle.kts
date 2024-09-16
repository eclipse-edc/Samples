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
 *
 */

plugins {
    `java-library`
}

dependencies {
    implementation(libs.edc.control.plane.core)
    implementation(libs.edc.control.plane.api.client)
    implementation(libs.edc.data.plane.core)
    implementation(libs.edc.data.plane.azure.storage)
    implementation(libs.edc.data.plane.aws.s3)
    implementation(libs.edc.data.plane.signaling.client)
    implementation(libs.edc.data.plane.selector.core)
    implementation(libs.edc.transfer.data.plane.signaling)

    implementation(libs.opentelemetry.annotations)
}
