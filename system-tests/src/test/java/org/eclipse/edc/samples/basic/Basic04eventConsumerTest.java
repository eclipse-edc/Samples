/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial test implementation for sample
 *       Mercedes-Benz Tech Innovation GmbH - refactor test cases
 *
 */

package org.eclipse.edc.samples.basic;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.CONSUMER_MANAGEMENT_URL;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.getConsumer;
import static org.eclipse.edc.samples.util.TransferUtil.post;

@EndToEndTest
class Basic04eventConsumerTest {

    private static final String CONNECTOR_MODULE_PATH = ":basic:basic-04-event-consumer:connector-with-event-subscriber";
    private static final String CREATE_ASSET_FILE_PATH = "basic/basic-04-event-consumer/resources/create-asset.json";
    private static final String V3_ASSETS_PATH = "/v3/assets";

    @RegisterExtension
    static RuntimeExtension connector = getConsumer(CONNECTOR_MODULE_PATH);

    @Test
    void runSampleSteps() {
        var standardOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(standardOutputStream));

        post(CONSUMER_MANAGEMENT_URL + V3_ASSETS_PATH, getFileContentFromRelativePath(CREATE_ASSET_FILE_PATH));

        var standardOutput = standardOutputStream.toString();
        assertThat(standardOutput).contains("Asset created:");
    }

}
