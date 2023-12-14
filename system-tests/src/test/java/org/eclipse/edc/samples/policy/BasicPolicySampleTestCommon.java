/*
 *  Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
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

package org.eclipse.edc.samples.policy;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.eclipse.edc.junit.testfixtures.TestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

/**
 * Encapsulates common settings, test steps, and helper methods for the test for {@code :policy:policy-01-contract-negotiation} sample.
 */
public class BasicPolicySampleTestCommon {
    private static final String INITIATE_CONTRACT_NEGOTIATION_URI = "http://localhost:9192/management/v2/contractnegotiations";
    private static final String LOOK_UP_CONTRACT_AGREEMENT_URI = "http://localhost:9192/management/v2/contractnegotiations/{id}";
    static final String API_KEY_HEADER_KEY = "X-Api-Key";
    static final String API_KEY_HEADER_VALUE = "password";
    String contractNegotiationId;

    /**
     * Creates a new {@link BasicPolicySampleTestCommon} instance.
     */
    public BasicPolicySampleTestCommon() {
    }

    /**
     * Resolves a {@link File} instance from a relative path.
     */
    @NotNull
    public static File getFileFromRelativePath(String relativePath) {
        return new File(TestUtils.findBuildRoot(), relativePath);
    }

    /**
     * Assert that a POST request to initiate a contract negotiation is successful.
     * This method corresponds to the command in the sample: {@code curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" -d @transfer/transfer-01-file-transfer/contractoffer.json "http://localhost:9192/api/v2/management/contractnegotiations"}
     */
    void initiateContractNegotiation(String contractOfferFilePath) {
        Response response = RestAssured
                .given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(new File(TestUtils.findBuildRoot(), contractOfferFilePath))
                .post(INITIATE_CONTRACT_NEGOTIATION_URI)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .response();

        String negotiationId = response.jsonPath().getString("@id");
        if (negotiationId == null || negotiationId.isEmpty()) {
            throw new IllegalStateException("Failed to get a valid contract negotiation ID from the response");
        }

        contractNegotiationId = negotiationId;
    }


    /**
     * Assert that a GET request to look up a contract agreement is successful and the {@code state} is {@code 'CONFIRMED'}.
     * This method corresponds to the command in the sample: {@code curl -X GET -H 'X-Api-Key: password' "http://localhost:9192/api/v2/management/contractnegotiations/{UUID}"}
     */
    void lookUpContractAgreementFinalized() {

        await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() ->
                RestAssured
                        .given()
                        .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                        .when()
                        .get(LOOK_UP_CONTRACT_AGREEMENT_URI, contractNegotiationId)
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .body("state", equalTo("FINALIZED"))
        );
    }

    void lookUpContractAgreementTerminated() {

        await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() ->
                RestAssured
                        .given()
                        .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                        .when()
                        .get(LOOK_UP_CONTRACT_AGREEMENT_URI, contractNegotiationId)
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .body("state", equalTo("TERMINATED"))
        );
    }
}