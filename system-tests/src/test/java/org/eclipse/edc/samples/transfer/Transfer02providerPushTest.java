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
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.samples.util.HttpRequestLoggerContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.NegotiationCommon.runNegotiation;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.getConsumer;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.getProvider;
import static org.eclipse.edc.samples.util.TransferUtil.checkTransferStatus;
import static org.eclipse.edc.samples.util.TransferUtil.startTransfer;

@EndToEndTest
@Testcontainers
public class Transfer02providerPushTest {

    private static final String START_TRANSFER_FILE_PATH = "transfer/transfer-02-provider-push/resources/start-transfer.json";

    @RegisterExtension
    static RuntimeExtension provider = getProvider();

    @RegisterExtension
    static RuntimeExtension consumer = getConsumer();

    @Container
    public static HttpRequestLoggerContainer httpRequestLoggerContainer = new HttpRequestLoggerContainer();

    @Test
    void runSampleSteps() {
        var contractAgreementId = runNegotiation();
        var requestBody = getFileContentFromRelativePath(START_TRANSFER_FILE_PATH)
                .replace("4000", String.valueOf(httpRequestLoggerContainer.getPort()));
        var transferProcessId = startTransfer(requestBody, contractAgreementId);
        checkTransferStatus(transferProcessId, TransferProcessStates.COMPLETED);
        assertThat(httpRequestLoggerContainer.getLog()).contains("Leanne Graham");
    }
}
