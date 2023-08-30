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

package org.eclipse.edc.samples.transfer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.eclipse.edc.connector.transfer.spi.types.DataRequest;
import org.eclipse.edc.junit.testfixtures.TestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates.COMPLETED;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Encapsulates common settings, test steps, and helper methods for transfer samples
 */
public class FileTransferSampleTestCommon {

    static final ObjectMapper MAPPER = new ObjectMapper();

    static final String MANAGEMENT_API_URL = "http://localhost:9192/management";
    static final String CONTRACT_OFFER_FILE_PATH = "transfer/transfer-01-file-transfer/contractoffer.json";
    static final String TRANSFER_FILE_PATH = "transfer/transfer-01-file-transfer/filetransfer.json";
    static final String API_KEY_HEADER_KEY = "X-Api-Key";
    static final String API_KEY_HEADER_VALUE = "password";

    final String sampleAssetFilePath;
    final File sampleAssetFile;
    final File destinationFile;
    Duration timeout = Duration.ofSeconds(30);
    Duration pollInterval = Duration.ofMillis(500);

    String contractNegotiationId;
    String contractAgreementId;

    /**
     * Creates a new {@code FileTransferSampleTestCommon} instance.
     *
     * @param sampleAssetFilePath Relative path starting from the root of the project to a file which will be read from for transfer.
     * @param destinationFilePath Relative path starting from the root of the project where the transferred file will be written to.
     */
    public FileTransferSampleTestCommon(@NotNull String sampleAssetFilePath, @NotNull String destinationFilePath) {
        this.sampleAssetFilePath = sampleAssetFilePath;
        sampleAssetFile = getFileFromRelativePath(sampleAssetFilePath);

        destinationFile = getFileFromRelativePath(destinationFilePath);
    }

    /**
     * Resolves a {@link File} instance from a relative path.
     */
    @NotNull
    public static File getFileFromRelativePath(String relativePath) {
        return new File(TestUtils.findBuildRoot(), relativePath);
    }

    /**
     * Resolves a {@link File} instance from a relative path.
     */
    @NotNull
    public static String getFileContentFromRelativePath(String relativePath) {
        var fileFromRelativePath = getFileFromRelativePath(relativePath);
        try {
            return Files.readString(Paths.get(fileFromRelativePath.toURI()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Assert that prerequisites are fulfilled before running the test.
     * This assertion checks only whether the file to be copied is not existing already.
     */
    void assertTestPrerequisites() {
        assertThat(destinationFile).doesNotExist();
    }

    /**
     * Remove files created while running the tests.
     * The copied file will be deleted.
     */
    void cleanTemporaryTestFiles() {
        destinationFile.delete();
    }

    /**
     * Assert that the file to be copied exists at the expected location.
     * This method waits a duration which is defined in {@link FileTransferSampleTestCommon#timeout}.
     */
    void assertDestinationFileContent() {
        await().atMost(timeout).pollInterval(pollInterval).untilAsserted(()
                -> assertThat(destinationFile).hasSameBinaryContentAs(sampleAssetFile));
    }

    /**
     * Assert that the transfer process state on the consumer is completed.
     */
    void assertTransferProcessStatusConsumerSide(String transferProcessId) {
        await().atMost(timeout).pollInterval(pollInterval).untilAsserted(() -> {
            var state = getTransferProcessState(transferProcessId);

            assertThat(state).isEqualTo(COMPLETED.name());
        });
    }

    /**
     * Assert that a POST request to initiate a contract negotiation is successful.
     * This method corresponds to the command in the sample: {@code curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" -d @transfer/transfer-01-file-transfer/contractoffer.json "http://localhost:9192/management/v2/contractnegotiations"}
     */
    void initiateContractNegotiation() {
        contractNegotiationId = given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(new File(TestUtils.findBuildRoot(), CONTRACT_OFFER_FILE_PATH))
                .when()
                .post(MANAGEMENT_API_URL + "/v2/contractnegotiations")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("@id", not(emptyString()))
                .extract()
                .jsonPath()
                .get("@id");
    }

    public String getTransferProcessState(String processId) {
        return given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .when()
                .get(String.format("%s/%s", MANAGEMENT_API_URL + "/v2/transferprocesses", processId))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().jsonPath().getString("'edc:state'");
    }

    /**
     * Assert that a GET request to look up a contract agreement is successful.
     * This method corresponds to the command in the sample: {@code curl -X GET -H 'X-Api-Key: password' "http://localhost:9192/management/v2/contractnegotiations/{UUID}"}
     */
    void lookUpContractAgreementId() {
        // Wait for transfer to be completed.
        await().atMost(timeout).pollInterval(pollInterval).untilAsserted(() -> contractAgreementId = given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .when()
                .get(MANAGEMENT_API_URL + "/v2/contractnegotiations/{id}", contractNegotiationId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("'edc:state'", equalTo("FINALIZED"))
                .body("'edc:contractAgreementId'", not(emptyString()))
                .extract().body().jsonPath().getString("'edc:contractAgreementId'")
        );
    }

    /**
     * Assert that a POST request to initiate transfer process is successful.
     * This method corresponds to the command in the sample: {@code curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" -d @transfer/transfer-01-file-transfer/filetransfer.json "http://localhost:9192/management/v2/transferprocesses"}
     *
     * @throws IOException Thrown if there was an error accessing the transfer request file defined in {@link FileTransferSampleTestCommon#TRANSFER_FILE_PATH}.
     */
    String requestTransferFile() throws IOException {
        var transferJsonFile = getFileFromRelativePath(TRANSFER_FILE_PATH);
        var requestBody = readAndUpdateDataRequestFromJsonFile(transferJsonFile, contractAgreementId);

        var jsonPath = given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(MANAGEMENT_API_URL + "/v2/transferprocesses")
                .then()
                .log().ifError()
                .statusCode(HttpStatus.SC_OK)
                .body("@id", not(emptyString()))
                .extract()
                .jsonPath();

        var transferProcessId = jsonPath.getString("@id");

        assertThat(transferProcessId).isNotEmpty();

        return transferProcessId;
    }

    /**
     * Reads a transfer request file with changed value for contract agreement ID and file destination path.
     *
     * @param transferJsonFile A {@link File} instance pointing to a JSON transfer request file.
     * @param contractAgreementId This string containing a UUID will be used as value for the contract agreement ID.
     * @return An instance of {@link DataRequest} with changed values for contract agreement ID and file destination path.
     * @throws IOException Thrown if there was an error accessing the file given in transferJsonFile.
     */
    Map<String, Object> readAndUpdateDataRequestFromJsonFile(@NotNull File transferJsonFile, @NotNull String contractAgreementId) throws IOException {
        var fileString = Files.readString(transferJsonFile.toPath())
                .replace("{path to destination file}", destinationFile.getAbsolutePath())
                .replace("{agreement ID}", contractAgreementId);

        return MAPPER.readValue(fileString, Map.class);
    }
}
