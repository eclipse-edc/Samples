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

import jakarta.json.Json;
import org.eclipse.edc.connector.controlplane.test.system.utils.Participant;

import java.util.List;
import java.util.UUID;

import static io.restassured.http.ContentType.JSON;
import static jakarta.json.Json.createArrayBuilder;
import static jakarta.json.Json.createObjectBuilder;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.CONTEXT;
import static org.eclipse.edc.jsonld.spi.JsonLdKeywords.ID;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_PREFIX;

/**
 * Essentially a wrapper around the management API enabling to test interactions with other participants, eg. catalog, transfer...
 */
public class StreamingParticipant extends Participant {

    protected Endpoint controlEndpoint;

    protected StreamingParticipant() {
    }

    public String getName() {
        return name;
    }

    public void registerDataPlane(List<String> sourceTypes, List<String> destinationTypes, List<Object> transferTypes) {
        var jsonObject = Json.createObjectBuilder()
                .add(CONTEXT, createObjectBuilder().add(EDC_PREFIX, EDC_NAMESPACE))
                .add(ID, UUID.randomUUID().toString())
                .add(EDC_NAMESPACE + "url", controlEndpoint.getUrl() + "/transfer")
                .add(EDC_NAMESPACE + "allowedSourceTypes", createArrayBuilder(sourceTypes))
                .add(EDC_NAMESPACE + "allowedDestTypes", createArrayBuilder(destinationTypes))
                .add(EDC_NAMESPACE + "allowedTransferTypes", createArrayBuilder(transferTypes))
                .build();

        managementEndpoint.baseRequest()
                .contentType(JSON)
                .body(jsonObject.toString())
                .when()
                .post("/v2/dataplanes")
                .then()
                .statusCode(200);
    }

    public String createAsset(String requestBody) {
        return managementEndpoint.baseRequest()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .post("/v3/assets")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract().jsonPath().getString(ID);
    }

    public String createPolicyDefinition(String requestBody) {
        return managementEndpoint.baseRequest()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .post("/v2/policydefinitions")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .extract().jsonPath().getString(ID);
    }

    public String createContractDefinition(String requestBody) {
        return managementEndpoint.baseRequest()
                .contentType(JSON)
                .body(requestBody)
                .when()
                .post("/v2/contractdefinitions")
                .then()
                .statusCode(200)
                .extract().jsonPath().getString(ID);
    }

    public static class Builder<P extends StreamingParticipant, B extends Builder<P, B>> extends Participant.Builder<P, B> {

        protected Builder(P participant) {
            super(participant);
        }

        public static <B extends Builder<StreamingParticipant, B>> Builder<StreamingParticipant, B> newStreamingInstance() {
            return new Builder<>(new StreamingParticipant());
        }

        public B controlEndpoint(Endpoint controlEndpoint) {
            participant.controlEndpoint = controlEndpoint;
            return self();
        }

        @Override
        public StreamingParticipant build() {
            return (StreamingParticipant) super.build();
        }
    }
}
