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

package org.eclipse.edc.samples.basic;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static io.restassured.RestAssured.given;
import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.containsString;

@EndToEndTest
class Basic02healthEndpointTest {

    @RegisterExtension
    static EdcRuntimeExtension controlPlane = new EdcRuntimeExtension(
            ":basic:basic-02-health-endpoint",
            "connector",
            emptyMap()
    );

    @Test
    void shouldStartConnector() {
        given()
                .get("http://localhost:8181/api/health")
                .then()
                .statusCode(200)
                .body(containsString("alive"));
    }
}
