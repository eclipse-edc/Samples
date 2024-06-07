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

package org.eclipse.edc.samples.transfer;

import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.eclipse.edc.samples.util.HttpRequestLoggerContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.NegotiationCommon.runNegotiation;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.getConsumer;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.getProvider;
import static org.eclipse.edc.samples.util.TransferUtil.checkTransferStatus;
import static org.eclipse.edc.samples.util.TransferUtil.startTransfer;

@EndToEndTest
public class Transfer04eventConsumerTest {
    private static final String CONSUMER_WITH_LISTENER_MODULE_PATH = ":transfer:transfer-04-event-consumer:consumer-with-listener";
    private static final String START_TRANSFER_FILE_PATH = "transfer/transfer-02-consumer-pull/resources/start-transfer.json";

    @RegisterExtension
    static EdcRuntimeExtension provider = getProvider();

    @RegisterExtension
    static EdcRuntimeExtension consumer = getConsumer(CONSUMER_WITH_LISTENER_MODULE_PATH);

    @Container
    static HttpRequestLoggerContainer httpRequestLoggerContainer = new HttpRequestLoggerContainer();

    @BeforeAll
    static void setUp() {
        httpRequestLoggerContainer.start();
    }

    @Test
    void runSampleSteps() {
        var standardOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(standardOutputStream));
        var requestBody = getFileContentFromRelativePath(START_TRANSFER_FILE_PATH);
        var contractAgreementId = runNegotiation();
        var transferProcessId = startTransfer(requestBody, contractAgreementId);
        checkTransferStatus(transferProcessId, TransferProcessStates.STARTED);
        var standardOutput = standardOutputStream.toString();
        assertThat(standardOutput).contains("TransferProcessStartedListener received STARTED event");
    }
}
