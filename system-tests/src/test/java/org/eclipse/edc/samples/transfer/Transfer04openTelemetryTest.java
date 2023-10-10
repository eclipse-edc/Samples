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

package org.eclipse.edc.samples.transfer;

import org.apache.http.HttpStatus;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.junit.jupiter.api.AfterEach;
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

@EndToEndTest
@Testcontainers
public class Transfer04openTelemetryTest {

    private static final String SAMPLE_FOLDER = "transfer/transfer-04-open-telemetry";
    private static final String DOCKER_COMPOSE_YAML = "/docker-compose.yaml";
    private static final String SAMPLE_ASSET_FILE_PATH = SAMPLE_FOLDER + "/README.md";
    private static final String DESTINATION_FILE_PATH = SAMPLE_FOLDER + "/README_transferred.md";
    private static final String CONTRACT_OFFER_FILE_PATH = SAMPLE_FOLDER + "/contractoffer.json";
    private static final String FILE_TRANSFER_FILE_PATH = SAMPLE_FOLDER + "/filetransfer.json";
    private static final String JAEGER_URL = "http://localhost:16686";

    private final FileTransferSampleTestCommon testUtils = new FileTransferSampleTestCommon(SAMPLE_ASSET_FILE_PATH, DESTINATION_FILE_PATH);

    @Container
    public static DockerComposeContainer<?> environment =
            new DockerComposeContainer<>(FileTransferSampleTestCommon.getFileFromRelativePath(SAMPLE_FOLDER + DOCKER_COMPOSE_YAML))
                    .withLocalCompose(true)
                    .waitingFor("consumer", Wait.forLogMessage(".*ready.*", 1));

    @BeforeAll
    static void setUp() {
        environment.start();
    }

    @Test
    void runSampleSteps() throws Exception {
        testUtils.assertTestPrerequisites();
        testUtils.initiateContractNegotiation(CONTRACT_OFFER_FILE_PATH);
        testUtils.lookUpContractAgreementId();
        var transferProcessId = testUtils.requestTransferFile(FILE_TRANSFER_FILE_PATH);
        testUtils.assertDestinationFileContent();
        testUtils.assertTransferProcessStatusConsumerSide(transferProcessId);
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

    @AfterEach
    protected void tearDown() {
        testUtils.cleanTemporaryTestFiles();
    }
}
