/*
 *  Copyright (c) 2023 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Sample workflow test
 *
 */

package org.eclipse.edc.samples.advanced.advanced00opentelemetry;

import org.apache.http.HttpStatus;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.eclipse.edc.samples.transfer.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.transfer.FileTransferCommon.getFileFromRelativePath;
import static org.eclipse.edc.samples.transfer.TransferUtil.checkTransferStatus;
import static org.eclipse.edc.samples.transfer.TransferUtil.startTransfer;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.runPrerequisites;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.createAsset;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.createContractDefinition;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.createPolicy;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.getContractAgreementId;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.negotiateContract;

@EndToEndTest
@Testcontainers
public class Advanced00openTelemetryTest {

    private static final String DOCKER_COMPOSE_YAML = "advanced/advanced-01-open-telemetry/docker-compose.yaml";
    private static final String NEGOTIATE_CONTRACT_FILE_PATH = "advanced/advanced-01-open-telemetry/resources/negotiate-contract.json";
    private static final String START_TRANSFER_FILE_PATH = "advanced/advanced-01-open-telemetry/resources/start-transfer.json";
    private static final String JAEGER_URL = "http://localhost:16686";

    @Container
    public static DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(getFileFromRelativePath(DOCKER_COMPOSE_YAML))
                    .withLocalCompose(true)
                    .waitingFor("consumer", Wait.forLogMessage(".*ready.*", 1));

    @BeforeAll
    static void setUp() {
        environment.start();
    }

    @Test
    void runSampleSteps()  {
        runPrerequisites();
        createAsset();
        createPolicy();
        createContractDefinition();
        var contractNegotiationId = negotiateContract(NEGOTIATE_CONTRACT_FILE_PATH);
        var contractAgreementId = getContractAgreementId(contractNegotiationId);
        var transferProcessId = startTransfer(getFileContentFromRelativePath(START_TRANSFER_FILE_PATH), contractAgreementId);
        checkTransferStatus(transferProcessId, TransferProcessStates.STARTED);
        assertJaegerState();
    }

    private void assertJaegerState() {
        try {
            var url = new URL(JAEGER_URL);
            var huc = (HttpURLConnection) url.openConnection();
            assertThat(huc.getResponseCode()).isEqualTo(HttpStatus.SC_OK);
        } catch (IOException e) {
            fail("Unable to assert Jaeger state", e);
        }
    }
}
