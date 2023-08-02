/*
 *  Copyright (c) 2023 imec
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       imec
 *
 */

package org.eclipse.edc.samples.transfer;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;



@EndToEndTest
@Testcontainers
public class Transfer04openTelemetryTest {

    private static final String SAMPLE_FOLDER = "transfer/transfer-04-open-telemetry";
    static final String CONTRACT_OFFER_FILE_PATH = SAMPLE_FOLDER + "/contractoffer.json";

    static final String TRANSFER_FILE_PATH = SAMPLE_FOLDER + "/filetransfer.json";

    static final String SAMPLE_ASSET_FILE_PATH = SAMPLE_FOLDER + "/README.md";
    static final String DESTINATION_FILE_PATH = "transfer/requested.test.txt";
    final FileTransferSampleTestCommon testUtils = new FileTransferSampleTestCommon(SAMPLE_ASSET_FILE_PATH, DESTINATION_FILE_PATH);

    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(FileTransferSampleTestCommon.getFileFromRelativePath("transfer/transfer-04-open-telemetry/docker-compose.yaml")).withLocalCompose(true)
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
        var transferProcessId = testUtils.requestTransferFile(TRANSFER_FILE_PATH);
        testUtils.assertDestinationFileContent();

        // testUtils.assertTransferProcessStatusConsumerSide(transferProcessId);
    }

    @AfterEach
    protected void tearDown() {
        testUtils.cleanTemporaryTestFiles();
    }
}
