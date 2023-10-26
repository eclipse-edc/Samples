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

package org.eclipse.edc.samples.transfer.transfer03providerpush;

import org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.eclipse.edc.samples.transfer.HttpRequestLoggerConsumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.samples.transfer.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.transfer.HttpRequestLoggerUtil.getHttpRequestLoggerContainer;
import static org.eclipse.edc.samples.transfer.TransferUtil.checkTransferStatus;
import static org.eclipse.edc.samples.transfer.TransferUtil.startTransfer;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.getConsumer;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.getProvider;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.runPrerequisites;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.runNegotiation;

@EndToEndTest
@Testcontainers
public class Transfer03providerPushTest {

    private static final HttpRequestLoggerConsumer LOG_CONSUMER = new HttpRequestLoggerConsumer();
    private static final String START_TRANSFER_FILE_PATH = "transfer/transfer-03-provider-push/resources/start-transfer.json";


    @RegisterExtension
    static EdcRuntimeExtension provider = getProvider();

    @RegisterExtension
    static EdcRuntimeExtension consumer = getConsumer();

    @Container
    public static DockerComposeContainer<?> httpRequestLoggerContainer = getHttpRequestLoggerContainer(LOG_CONSUMER);

    @BeforeAll
    static void setUp() {
        httpRequestLoggerContainer.start();
    }

    @Test
    void runSampleSteps() {
        runPrerequisites();
        var contractAgreementId = runNegotiation();
        var requestBody = getFileContentFromRelativePath(START_TRANSFER_FILE_PATH);
        var transferProcessId = startTransfer(requestBody, contractAgreementId);
        checkTransferStatus(transferProcessId, TransferProcessStates.COMPLETED);
        assertThat(LOG_CONSUMER.toUtf8String()).contains("Leanne Graham");
    }
}
