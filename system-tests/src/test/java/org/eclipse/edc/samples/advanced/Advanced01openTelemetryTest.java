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
 *       Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V. - use current ids instead of placeholder
 *
 */

package org.eclipse.edc.samples.advanced;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates.STARTED;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;
import static org.eclipse.edc.samples.common.NegotiationCommon.createAsset;
import static org.eclipse.edc.samples.common.NegotiationCommon.createContractDefinition;
import static org.eclipse.edc.samples.common.NegotiationCommon.createPolicy;
import static org.eclipse.edc.samples.common.NegotiationCommon.fetchDatasetFromCatalog;
import static org.eclipse.edc.samples.common.NegotiationCommon.getContractAgreementId;
import static org.eclipse.edc.samples.common.NegotiationCommon.negotiateContract;
import static org.eclipse.edc.samples.util.TransferUtil.checkTransferStatus;
import static org.eclipse.edc.samples.util.TransferUtil.startTransfer;

@EndToEndTest
@Testcontainers
public class Advanced01openTelemetryTest {

    private static final String SAMPLE_FOLDER = "advanced/advanced-01-open-telemetry";
    private static final String DOCKER_COMPOSE_YAML = SAMPLE_FOLDER + "/docker-compose.yaml";
    private static final String FETCH_DATASET_FROM_CATALOG_FILE_PATH = SAMPLE_FOLDER + "/resources/get-dataset.json";
    private static final String NEGOTIATE_CONTRACT_FILE_PATH = SAMPLE_FOLDER + "/resources/negotiate-contract.json";
    private static final String START_TRANSFER_FILE_PATH = SAMPLE_FOLDER + "/resources/start-transfer.json";
    private static final String JAEGER_URL = "http://localhost:16686";

    @Container
    public ComposeContainer environment =
            new ComposeContainer(getFileFromRelativePath(DOCKER_COMPOSE_YAML))
                    .withLocalCompose(true)
                    .waitingFor("consumer", Wait.forLogMessage(".*ready.*", 1));

    @Test
    void runSampleSteps()  {
        createAsset();
        createPolicy();
        createContractDefinition();
        var catalogDatasetId = fetchDatasetFromCatalog(FETCH_DATASET_FROM_CATALOG_FILE_PATH);
        var contractNegotiationId = negotiateContract(NEGOTIATE_CONTRACT_FILE_PATH, catalogDatasetId);
        var contractAgreementId = getContractAgreementId(contractNegotiationId);
        var transferRequest = getFileContentFromRelativePath(START_TRANSFER_FILE_PATH);
        var transferProcessId = startTransfer(transferRequest, contractAgreementId);
        checkTransferStatus(transferProcessId, STARTED);

        given()
                .baseUri(JAEGER_URL)
                .get()
                .then()
                .statusCode(200);
    }
}
