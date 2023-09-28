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
import org.eclipse.edc.spi.EdcException;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;


@EndToEndTest
@Testcontainers
public class Transfer04openTelemetryTest {

    private static final String OPEN_TELEMETRY_JAR_URL = "https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.27.0/opentelemetry-javaagent.jar";
    private static final String SAMPLE_FOLDER = "transfer/transfer-04-open-telemetry";
    private static final String CONTRACT_OFFER_FILE_PATH = SAMPLE_FOLDER + "/contractoffer.json";
    private static final String OPEN_TELEMETRY_JAR_PATH = SAMPLE_FOLDER + "/opentelemetry-javaagent.jar";
    private static final String TRANSFER_FILE_PATH = SAMPLE_FOLDER + "/filetransfer.json";
    private static final String SAMPLE_ASSET_FILE_PATH = SAMPLE_FOLDER + "/README.md";
    private static final String DESTINATION_FILE_PATH = "transfer/requested.test.txt";
    final FileTransferSampleTestCommon testUtils = new FileTransferSampleTestCommon(SAMPLE_ASSET_FILE_PATH, DESTINATION_FILE_PATH);

    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(FileTransferSampleTestCommon.getFileFromRelativePath("transfer/transfer-04-open-telemetry/docker-compose.yaml")).withLocalCompose(true)
            .waitingFor("consumer", Wait.forLogMessage(".*ready.*", 1));

    @BeforeAll
    static void setUp() {
        downloadOpenTelemetryJar();
        environment.start();
    }

    @Test
    void runSampleSteps() throws Exception {
        testUtils.assertTestPrerequisites();
        testUtils.initiateContractNegotiation(CONTRACT_OFFER_FILE_PATH);
        testUtils.lookUpContractAgreementId();
        var transferProcessId = testUtils.requestTransferFile(TRANSFER_FILE_PATH);
        testUtils.assertDestinationFileContent();
        // next one fails with "No checker found for process error (related to https://github.com/eclipse-edc/Connector/issues/3334 ?)
        // testUtils.assertTransferProcessStatusConsumerSide(transferProcessId);
    }

    @AfterEach
    protected void tearDown() {
        testUtils.cleanTemporaryTestFiles();
    }

    private static void downloadOpenTelemetryJar() {
        try (var in = new BufferedInputStream(new URL(OPEN_TELEMETRY_JAR_URL).openStream());
             var out = new FileOutputStream("../" + OPEN_TELEMETRY_JAR_PATH)) {
            var dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                out.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new EdcException(e);
        }
    }
}
