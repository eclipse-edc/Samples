/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.sample.extension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.apache.http.HttpStatus;
import org.eclipse.edc.connector.api.management.transferprocess.model.TransferProcessDto;
import org.eclipse.edc.connector.transfer.spi.types.DataRequest;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.junit.testfixtures.TestUtils;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class PolicyProvisionSampleTestCommon {
    static final ObjectMapper MAPPER = new ObjectMapper();
    //region constant test settings
    static final String INITIATE_CONTRACT_NEGOTIATION_URI = "http://localhost:9192/api/v1/management/contractnegotiations";
    static final String LOOK_UP_CONTRACT_AGREEMENT_URI = "http://localhost:9192/api/v1/management/contractnegotiations/{id}";
    static final String TRANSFER_PROCESS_URI = "http://localhost:9192/api/v1/management/transferprocess";
    static final String API_KEY_HEADER_KEY = "X-Api-Key";
    static final String API_KEY_HEADER_VALUE = "password";
    //endregion

    //region changeable test settings
    final String sampleAssetFilePath;
    final File sampleAssetFile;
    final String destinationFilePath;
    final File destinationFile;
    final String desiredDestinationFilePath;
    final File desiredDestinationFile;
    Duration timeout = Duration.ofSeconds(60);
    Duration pollInterval = Duration.ofMillis(500);
    //endregion

    String contractNegotiationId;
    String contractAgreementId;

    /**
     * Creates a new {@link PolicyProvisionSampleTestCommon} instance.
     */
    public PolicyProvisionSampleTestCommon(@NotNull String sampleAssetFilePath, @NotNull String destinationFilePath, @NotNull String desiredDestinationFilePath) {
        this.sampleAssetFilePath = sampleAssetFilePath;
        sampleAssetFile = getFileFromRelativePath(sampleAssetFilePath);

        this.destinationFilePath = destinationFilePath;
        destinationFile = getFileFromRelativePath(destinationFilePath);

        this.desiredDestinationFilePath = desiredDestinationFilePath;
        desiredDestinationFile = getFileFromRelativePath(desiredDestinationFilePath);
    }

    /**
     * Resolves a {@link File} instance from a relative path.
     */
    @NotNull
    public static File getFileFromRelativePath(String relativePath) {
        return new File(TestUtils.findBuildRoot(), relativePath);
    }

    /**
     * Assert that prerequisites are fulfilled before running the test.
     * This assertion checks only whether the file to be copied is not existing already.
     */
    void assertTestPrerequisites() {
        assertThat(destinationFile).doesNotExist();
        assertThat(desiredDestinationFile).doesNotExist();
    }

    /**
     * Remove files created while running the tests.
     * The copied file will be deleted.
     */
    void cleanTemporaryTestFiles() {
        destinationFile.delete();
        desiredDestinationFile.delete();
    }

    /**
     * Assert that the file to be copied exists at the expected location.
     * This method waits a duration which is defined in {@link PolicyProvisionSampleTestCommon#timeout}.
     */
    void assertDestinationFileContent() {
        await().atMost(timeout).pollInterval(pollInterval).untilAsserted(()
                -> assertThat(desiredDestinationFile).hasSameBinaryContentAs(sampleAssetFile));
    }

    /**
     * Assert that there is no file in location {@link PolicyProvisionSampleTestCommon#destinationFilePath}
     * This method waits a duration which is defined in {@link PolicyProvisionSampleTestCommon#timeout}.
     */
    void assertFileDoesNotExist() {
        await().atMost(timeout).pollInterval(pollInterval).untilAsserted(()
                -> assertThat(destinationFile).doesNotExist());
    }

    /**
     * Assert that the transfer process state on the consumer is completed.
     */
    void assertTransferProcessStatusConsumerSide(String transferProcessId) {
        await().atMost(timeout).pollInterval(pollInterval).untilAsserted(()
                -> {
            var transferProcess = getTransferProcessById(transferProcessId);

            //as policy sample file transfer does not use any status checker yet, it will not update to 'COMPLETED' state.
            //for now we will just check if the state is 'IN_PROGRESS'
            // TODO: should be changed to 'COMPLETED' once the status checker is implemented in the module
            assertThat(transferProcess).extracting(TransferProcessDto::getState).isEqualTo(TransferProcessStates.IN_PROGRESS.toString());
        });
    }

    /**
     * Gets the transfer process by ID.
     *
     * @return The transfer process.
     */
    public TransferProcessDto getTransferProcessById(String processId) {
        return RestAssured.given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .when()
                .get(String.format("%s/%s", TRANSFER_PROCESS_URI, processId))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().body().as(TransferProcessDto.class);
    }

    /**
     * Creates a policy that matches the policy used by provider connector.
     *
     * @return The suitable {@link Policy}.
     */
    private Policy createContractPolicy() {

        var regulateFilePathConstraint = AtomicConstraint.Builder.newInstance()
                .leftExpression(new LiteralExpression("POLICY_REGULATE_FILE_PATH"))
                .operator(Operator.EQ)
                .rightExpression(new LiteralExpression(getFileFromRelativePath(desiredDestinationFilePath).getAbsolutePath()))
                .build();


        var permission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type("USE").build())
                .constraint(regulateFilePathConstraint)
                .target("test-document")
                .build();

        return Policy.Builder.newInstance()
                .permission(permission)
                .build();
    }

    /**
     * Assert that a POST request to initiate a contract negotiation is successful.
     * This method corresponds to the command in the sample: {@code curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" -d @policy/policy-02-provision/contractoffer.json "http://localhost:9192/api/v1/management/contractnegotiations"}
     *
     * @param contractOfferFilePath A {@link File} instance pointing to a JSON contract offer file.
     */
    void initiateContractNegotiation(String contractOfferFilePath) throws IOException {

        var contractOfferFile = new File(TestUtils.findBuildRoot(), contractOfferFilePath);

        // get the root node from contractoffer.json
        ObjectNode contractOfferJsonRootNode = MAPPER.readValue(contractOfferFile, ObjectNode.class);

        // adjust the policy to match provider's policy
        ObjectNode offerNode = (ObjectNode) contractOfferJsonRootNode.get("offer");
        offerNode.putPOJO("policy", createContractPolicy());

        contractNegotiationId = RestAssured
                .given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(contractOfferJsonRootNode)
                .when()
                .post(INITIATE_CONTRACT_NEGOTIATION_URI)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", not(emptyString()))
                .extract()
                .jsonPath()
                .get("id");
    }

    /**
     * Assert that a GET request to look up a contract agreement is successful.
     * This method corresponds to the command in the sample: {@code curl -X GET -H 'X-Api-Key: password' "http://localhost:9192/api/v1/management/contractnegotiations/{UUID}"}
     */
    void lookUpContractAgreementId() {
        // Wait for transfer to be completed.
        await().atMost(timeout).pollInterval(pollInterval).untilAsserted(() -> contractAgreementId = RestAssured
                .given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .when()
                .get(LOOK_UP_CONTRACT_AGREEMENT_URI, contractNegotiationId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("state", equalTo("CONFIRMED"))
                .body("contractAgreementId", not(emptyString()))
                .extract().body().jsonPath().getString("contractAgreementId")
        );
    }

    /**
     * Assert that a POST request to initiate transfer process is successful.
     * This method corresponds to the command in the sample: {@code curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" -d @filetransfer.json "http://localhost:9192/api/v1/management/transferprocess"}
     *
     * @param transferFilePath A {@link File} instance pointing to a JSON transfer request file.
     * @throws IOException Thrown if there was an error accessing the transfer request file defined in transferFilePath.
     */
    String requestTransferFile(String transferFilePath) throws IOException {
        var transferJsonFile = getFileFromRelativePath(transferFilePath);
        DataRequest sampleDataRequest = readAndUpdateDataRequestFromJsonFile(transferJsonFile, contractAgreementId);

        JsonPath jsonPath = RestAssured
                .given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(sampleDataRequest)
                .when()
                .post(TRANSFER_PROCESS_URI)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", not(emptyString()))
                .extract()
                .jsonPath();

        String transferProcessId = jsonPath.get("id");

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
    DataRequest readAndUpdateDataRequestFromJsonFile(@NotNull File transferJsonFile, @NotNull String contractAgreementId) throws IOException {
        // convert JSON file to map
        DataRequest sampleDataRequest = MAPPER.readValue(transferJsonFile, DataRequest.class);

        var changedAddressProperties = sampleDataRequest.getDataDestination().getProperties();
        changedAddressProperties.put("path", destinationFile.getAbsolutePath());

        DataAddress newDataDestination = DataAddress.Builder.newInstance()
                .properties(changedAddressProperties)
                .build();

        return DataRequest.Builder.newInstance()
                // copy unchanged values from JSON file
                .id(sampleDataRequest.getId())
                .processId(sampleDataRequest.getProcessId())
                .connectorAddress(sampleDataRequest.getConnectorAddress())
                .protocol(sampleDataRequest.getProtocol())
                .connectorId(sampleDataRequest.getConnectorId())
                .assetId(sampleDataRequest.getAssetId())
                .destinationType(sampleDataRequest.getDestinationType())
                .transferType(sampleDataRequest.getTransferType())
                .managedResources(sampleDataRequest.isManagedResources())
                .properties(sampleDataRequest.getProperties())
                // set changed values
                .contractId(contractAgreementId)
                .dataDestination(newDataDestination)
                .build();
    }
}