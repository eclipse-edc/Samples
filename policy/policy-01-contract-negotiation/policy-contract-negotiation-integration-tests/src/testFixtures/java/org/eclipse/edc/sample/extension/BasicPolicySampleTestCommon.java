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

package org.eclipse.edc.sample.extension;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.eclipse.edc.junit.testfixtures.TestUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Encapsulates common settings, test steps, and helper methods for the test for {@code :policy:policy-01-contract-negotiation} sample.
 */
public class BasicPolicySampleTestCommon {

    //region constant test settings
    static final String INITIATE_CONTRACT_NEGOTIATION_URI = "http://localhost:8182/api/v1/management/contractnegotiations";
    static final String LOOK_UP_CONTRACT_AGREEMENT_URI = "http://localhost:8182/api/v1/management/contractnegotiations/{id}";
    static final String API_KEY_HEADER_KEY = "X-Api-Key";
    static final String API_KEY_HEADER_VALUE = "password";
    //endregion

    //region changeable test settings
    Duration timeout = Duration.ofSeconds(30);
    Duration pollInterval = Duration.ofMillis(500);
    //endregion

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
     * This method corresponds to the command in the sample: {@code curl -X POST -H "Content-Type: application/json" -H "X-Api-Key: password" -d @transfer/transfer-01-file-transfer/contractoffer.json "http://localhost:9192/api/v1/management/contractnegotiations"}
     */
    void initiateContractNegotiation(String contractOfferFilePath) {
        contractNegotiationId = RestAssured
                .given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(new File(TestUtils.findBuildRoot(), contractOfferFilePath))
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
     * Assert that a GET request to look up a contract agreement is successful and the {@code state} is {@code 'CONFIRMED'}.
     * This method corresponds to the command in the sample: {@code curl -X GET -H 'X-Api-Key: password' "http://localhost:9192/api/v1/management/contractnegotiations/{UUID}"}
     */
    void lookUpContractAgreementConfirmed() {
        // Wait for transfer to be completed.
        await().atMost(timeout).pollInterval(pollInterval).untilAsserted(() -> RestAssured
                .given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .when()
                .get(LOOK_UP_CONTRACT_AGREEMENT_URI, contractNegotiationId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("state", equalTo("CONFIRMED"))
        );
    }

    /**
     * Assert that a GET request to look up a contract agreement is successful and the {@code state} is {@code 'DECLINED'}.
     * This method corresponds to the command in the sample: {@code curl -X GET -H 'X-Api-Key: password' "http://localhost:9192/api/v1/management/contractnegotiations/{UUID}"}
     */
    void lookUpContractAgreementDeclined() {
        // Wait for transfer to be completed.
        await().atMost(Duration.ofSeconds(120)).pollInterval(pollInterval).untilAsserted(() -> RestAssured
                .given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .when()
                .get(LOOK_UP_CONTRACT_AGREEMENT_URI, contractNegotiationId)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("state", equalTo("DECLINED"))
        );
    }
}