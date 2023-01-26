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

val groupId: String by project
val edcVersion: String by project

dependencies {
    implementation("$groupId:control-plane-core:$edcVersion")
    implementation("$groupId:data-plane-core:$edcVersion")
    implementation("$groupId:data-plane-azure-storage:$edcVersion")
    implementation("$groupId:data-plane-aws-s3:$edcVersion")
    implementation("$groupId:data-plane-client:$edcVersion")
    implementation("$groupId:data-plane-selector-client:$edcVersion")
    implementation("$groupId:data-plane-selector-core:$edcVersion")
    implementation("$groupId:transfer-data-plane:$edcVersion")

    implementation(libs.opentelemetry.annotations)
}