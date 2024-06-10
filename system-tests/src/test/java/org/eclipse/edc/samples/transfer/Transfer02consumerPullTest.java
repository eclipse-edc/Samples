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
 *       Mercedes-Benz Tech Innovation GmbH - refactor test cases
 *
 */

package org.eclipse.edc.samples.transfer;

import io.restassured.common.mapper.TypeRef;
import org.apache.http.HttpStatus;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.NegotiationCommon.runNegotiation;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.API_KEY_HEADER_KEY;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.API_KEY_HEADER_VALUE;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.CONSUMER_MANAGEMENT_URL;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.getConsumer;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.getProvider;
import static org.eclipse.edc.samples.util.TransferUtil.checkTransferStatus;
import static org.eclipse.edc.samples.util.TransferUtil.startTransfer;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;

@EndToEndTest
@Testcontainers
public class Transfer02consumerPullTest {

    private static final String START_TRANSFER_FILE_PATH = "transfer/transfer-02-consumer-pull/resources/start-transfer.json";

    @RegisterExtension
    static EdcRuntimeExtension provider = getProvider();

    @RegisterExtension
    static EdcRuntimeExtension consumer = getConsumer();

    @Test
    void runSampleSteps() {
        var requestBody = getFileContentFromRelativePath(START_TRANSFER_FILE_PATH);
        var contractAgreementId = runNegotiation();
        var transferProcessId = startTransfer(requestBody, contractAgreementId);
        checkTransferStatus(transferProcessId, TransferProcessStates.STARTED);

        var edr = given()
                .when()
                .get(CONSUMER_MANAGEMENT_URL + "/v1/edrs/{id}/dataaddress", transferProcessId)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .extract().body().as(new TypeRef<Map<String, Object>>() {
                });

        var result = given()
                .header(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .header(AUTHORIZATION, edr.get("authorization"))
                .when()
                .get(edr.get("endpoint").toString())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log().ifValidationFails()
                .body("[0].name", not(emptyString()))
                .extract()
                .jsonPath()
                .get("[0].name");

        assertThat(result).isEqualTo("Leanne Graham");
    }

}
