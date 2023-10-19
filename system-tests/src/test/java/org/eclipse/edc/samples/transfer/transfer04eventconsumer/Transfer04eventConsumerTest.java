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
 *
 */

package org.eclipse.edc.samples.transfer.transfer04eventconsumer;

import org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.samples.transfer.HttpRequestLoggerUtil.getHttpRequestLoggerContainer;
import static org.eclipse.edc.samples.transfer.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.transfer.TransferUtil.checkTransferStatus;
import static org.eclipse.edc.samples.transfer.TransferUtil.startTransfer;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.*;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.*;

@EndToEndTest
public class Transfer04eventConsumerTest {
    private static final String CONNECTOR_MODULE_PATH = ":transfer:transfer-04-event-consumer:consumer-with-listener";
    private static final String START_TRANSFER_FILE_PATH = "transfer/transfer-02-consumer-pull/resources/start-transfer.json";

    @RegisterExtension
    static EdcRuntimeExtension provider = getProvider();

    @RegisterExtension
    static EdcRuntimeExtension consumer = getConsumer(CONNECTOR_MODULE_PATH);

    @Container
    static DockerComposeContainer<?> httpRequestLoggerContainer = getHttpRequestLoggerContainer();

    @BeforeAll
    static void setUp() {
        httpRequestLoggerContainer.start();
    }

    @Test
    void runSampleSteps() {
        var standardOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(standardOutputStream));
        runPrerequisites();
        var requestBody = getFileContentFromRelativePath(START_TRANSFER_FILE_PATH);
        var contractAgreementId = runNegotiation();
        var transferProcessId = startTransfer(requestBody, contractAgreementId);
        checkTransferStatus(transferProcessId, TransferProcessStates.STARTED);
        var standardOutput = standardOutputStream.toString();
        assertThat(standardOutput).contains("TransferProcessStartedListener received STARTED event");
    }
}
