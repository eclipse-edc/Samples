/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.samples.transfer.streaming;

import org.eclipse.edc.connector.controlplane.test.system.utils.LazySupplier;
import org.eclipse.edc.connector.controlplane.test.system.utils.Participant;

import java.net.URI;

import static io.restassured.http.ContentType.JSON;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;

/**
 * Essentially a wrapper around the management API enabling to test interactions with other participants, eg. catalog, transfer...
 */
public class StreamingParticipant extends Participant {

    protected StreamingParticipant() {
    }

    public String getName() {
        return name;
    }

    public String createAsset(String requestBody) {
        return baseManagementRequest()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .header("x-api-key", "bau")
                .post("/v3/assets")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .contentType(JSON)
                .extract().jsonPath().getString(ID);
    }

    public String createPolicyDefinition(String requestBody) {
        return baseManagementRequest()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .post("/v3/policydefinitions")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract().jsonPath().getString(ID);
    }

    public String createContractDefinition(String requestBody) {
        return baseManagementRequest()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .post("/v3/contractdefinitions")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString(ID);
    }

    public String fetchDatasetFromCatalog(String requestBody) {
        return baseManagementRequest()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .post("/v3/catalog/dataset/request")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract().jsonPath().getString("'odrl:hasPolicy'.@id");
    }

    public String negotiateContract(String requestBody) {
        return baseManagementRequest()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .post("/v3/contractnegotiations/")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract().jsonPath().getString(ID);
    }

    public String getContractAgreementId(String contractNegotiationId) {
        return baseManagementRequest()
                .contentType(JSON)
                .when()
                .get("/v3/contractnegotiations/" + contractNegotiationId)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract().jsonPath().getString("contractAgreementId");
    }

    public String startTransfer(String requestBody) {
        return baseManagementRequest()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .post("/v3/transferprocesses")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract().jsonPath().getString(ID);
    }

    public static class Builder<P extends StreamingParticipant, B extends Builder<P, B>> extends Participant.Builder<P, B> {

        protected Builder(P participant) {
            super(participant);
        }

        public static <B extends Builder<StreamingParticipant, B>> Builder<StreamingParticipant, B> newStreamingInstance() {
            return new Builder<>(new StreamingParticipant());
        }

        public B controlPlaneManagement(LazySupplier<URI> controlPlaneManagement) {
            participant.controlPlaneManagement = controlPlaneManagement;
            return self();
        }

        public B controlPlaneProtocol(LazySupplier<URI> controlPlaneProtocol) {
            participant.controlPlaneProtocol = controlPlaneProtocol;
            return self();
        }

    }

}
