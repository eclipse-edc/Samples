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

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static java.util.Collections.emptyMap;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.transfer.FileTransferSampleTestCommon.API_KEY_HEADER_KEY;
import static org.eclipse.edc.samples.transfer.FileTransferSampleTestCommon.API_KEY_HEADER_VALUE;
import static org.eclipse.edc.samples.transfer.FileTransferSampleTestCommon.MANAGEMENT_API_URL;
import static org.eclipse.edc.samples.transfer.FileTransferSampleTestCommon.getFileFromRelativePath;
import static org.hamcrest.CoreMatchers.is;

@EndToEndTest
public class Transfer03modifyTransferProcessTest {

    static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-03-modify-transferprocess/modify-transferprocess-consumer/config.properties";
    static final Duration DURATION = Duration.ofSeconds(15);
    static final Duration POLL_INTERVAL = Duration.ofMillis(500);

    @RegisterExtension
    static EdcRuntimeExtension consumer = new EdcRuntimeExtension(
            ":transfer:transfer-03-modify-transferprocess:modify-transferprocess-consumer",
            "consumer",
            Map.of(
                    "edc.fs.config", getFileFromRelativePath(CONSUMER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
            )
    );

    /**
     * Requests transfer processes from management API and check for expected changes on the transfer process.
     */
    @Test
    void runSample() {
        await().atMost(DURATION).pollInterval(POLL_INTERVAL).untilAsserted(() -> {
            given()
                    .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                    .when()
                    .contentType(ContentType.JSON)
                    .body(emptyMap())
                    .post((MANAGEMENT_API_URL + "/v2/transferprocesses") + "/request")
                    .then()
                    .statusCode(HttpStatus.SC_OK)
                    .contentType(ContentType.JSON)
                    .body("[0].@id", is("tp-sample-transfer-03"))
                    .body("[0].'edc:state'", is("TERMINATING"))
                    .body("[0].'edc:errorDetail'", is("timeout by watchdog"));
        });
    }
}
