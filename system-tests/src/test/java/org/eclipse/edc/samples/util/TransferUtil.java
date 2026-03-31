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
 *       Mercedes-Benz Tech Innovation GmbH - Initial implementation
 *       Fraunhofer-Gesellschaft zur Förderung der angewandten Forschung e.V. - use current ids instead of placeholder
 *
 */

package org.eclipse.edc.samples.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates;

import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.API_KEY_HEADER_KEY;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.API_KEY_HEADER_VALUE;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.CONSUMER_MANAGEMENT_URL;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;

public class TransferUtil {

    public static final Duration TIMEOUT = Duration.ofSeconds(30);
    public static final Duration POLL_DELAY = Duration.ofMillis(1000);
    public static final Duration POLL_INTERVAL = Duration.ofMillis(500);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String CONTRACT_AGREEMENT_ID_KEY = "{{contract-agreement-id}}";
    private static final String V2_TRANSFER_PROCESSES_PATH = "/v3/transferprocesses/";
    private static final String EDC_STATE = "state";

    public static void get(String url) {
        given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .log().ifError()
                .statusCode(HttpStatus.SC_OK);
    }

    public static String get(String url, String jsonPath) {
        return given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .when()
                .get(url)
                .then()
                .log().ifError()
                .statusCode(HttpStatus.SC_OK)
                .body(jsonPath, not(emptyString()))
                .extract()
                .jsonPath()
                .get(jsonPath);
    }

    public static ValidatableResponse post(String url, String requestBody) {
        return given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(url)
                .then()
                .log().ifValidationFails()
                .statusCode(HttpStatus.SC_OK);
    }

    public static String post(String url, String requestBody, String jsonPath) {
        return post(url, requestBody)
                .body(jsonPath, not(emptyString()))
                .extract()
                .jsonPath()
                .get(jsonPath);
    }

    public static String extractContractOfferId(ValidatableResponse response) {
        try {
            var body = response.extract().asString();
            var root = OBJECT_MAPPER.readTree(body);
            var offerId = readPolicyId(root, "odrl:hasPolicy");
            if (offerId != null) {
                return offerId;
            }

            offerId = readPolicyId(root, "hasPolicy");
            if (offerId != null) {
                return offerId;
            }

            offerId = readPolicyId(root, "dcat:dataset", "odrl:hasPolicy");
            if (offerId != null) {
                return offerId;
            }

            offerId = readPolicyId(root, "dcat:dataset", "hasPolicy");
            if (offerId != null) {
                return offerId;
            }

            throw new IllegalStateException("Cannot extract contract offer id from response: " + body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String startTransfer(String requestBody, String contractAgreementId) {
        requestBody = requestBody.replace(CONTRACT_AGREEMENT_ID_KEY, contractAgreementId);
        return post(CONSUMER_MANAGEMENT_URL + V2_TRANSFER_PROCESSES_PATH, requestBody, ID);
    }

    public static void checkTransferStatus(String transferProcessId, TransferProcessStates status) {
        await()
                .atMost(TIMEOUT)
                .pollDelay(POLL_DELAY)
                .pollInterval(POLL_INTERVAL)
                .untilAsserted(() -> {
                    var state = get(CONSUMER_MANAGEMENT_URL + V2_TRANSFER_PROCESSES_PATH + transferProcessId, EDC_STATE);
                    assertThat(state).isEqualTo(status.name());
                });
    }

    private static String readText(JsonNode root, String... path) {
        var node = root;
        for (var key : path) {
            node = node.path(key);
        }
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private static String readPolicyId(JsonNode root, String... path) {
        var node = root;
        for (var key : path) {
            node = node.path(key);
        }
        if (node.isArray()) {
            node = node.isEmpty() ? null : node.get(0);
        }
        return node == null ? null : readText(node, ID);
    }
}
