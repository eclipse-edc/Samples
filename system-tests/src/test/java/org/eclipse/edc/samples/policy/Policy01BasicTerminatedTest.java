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

package org.eclipse.edc.samples.policy;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.eclipse.edc.junit.testfixtures.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.IsEqual.equalTo;

@EndToEndTest
public class Policy01BasicTerminatedTest {

    static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-01-policy-enforcement/policy-enforcement-provider/config.properties";
    static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-01-policy-enforcement/policy-enforcement-consumer/config-us.properties";
    static final String CONTRACT_OFFER_FILE_PATH = "policy/policy-01-policy-enforcement/contractoffer.json";

    @RegisterExtension
    static EdcRuntimeExtension provider = new EdcRuntimeExtension(
            ":policy:policy-01-policy-enforcement:policy-enforcement-provider",
            "provider",
            Map.of(
                    "edc.fs.config", getFileFromRelativePath(PROVIDER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
            )
    );

    @RegisterExtension
    static EdcRuntimeExtension consumer = new EdcRuntimeExtension(
            ":policy:policy-01-policy-enforcement:policy-enforcement-consumer",
            "consumer",
            Map.of(
                    "edc.fs.config", getFileFromRelativePath(CONSUMER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
            )
    );

    String contractNegotiationId;

    @Test
    void runSampleSteps() {
        initiateContractNegotiation(CONTRACT_OFFER_FILE_PATH);
        lookUpContractAgreementTerminated();
    }

    @NotNull
    public static File getFileFromRelativePath(String relativePath) {
        return new File(TestUtils.findBuildRoot(), relativePath);
    }

    void initiateContractNegotiation(String contractOfferFilePath) {
        Response response = RestAssured
                .given()
                .headers("X-Api-Key", "password")
                .contentType(ContentType.JSON)
                .body(new File(TestUtils.findBuildRoot(), contractOfferFilePath))
                .post("http://localhost:9192/management/v2/contractnegotiations")
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

    void lookUpContractAgreementTerminated() {
        await().atMost(120, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).untilAsserted(() ->
                RestAssured
                        .given()
                        .headers("X-Api-Key", "password")
                        .when()
                        .get("http://localhost:9192/management/v2/contractnegotiations/{id}", contractNegotiationId)
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .body("state", equalTo("TERMINATED"))
        );
    }
}
