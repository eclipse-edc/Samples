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

package org.eclipse.edc.samples.transfer.transfer02consumerpull;

import org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.eclipse.edc.samples.transfer.HttpRequestLoggerConsumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.HttpRequestLoggerUtil.getHttpRequestLoggerContainer;
import static org.eclipse.edc.samples.transfer.TransferUtil.*;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.*;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.*;
import static org.eclipse.edc.samples.transfer.transfer02consumerpull.ConsumerPullCommon.startConsumerPullTransfer;
import static org.hamcrest.Matchers.not;

@EndToEndTest
@Testcontainers
public class Transfer02consumerPullTest {

    private static final HttpRequestLoggerConsumer LOG_CONSUMER = new HttpRequestLoggerConsumer();
    private static final String MANAGEMENT_V2_TRANSFER_PROCESSES_PATH = "/management/v2/transferprocesses/";
    private static final String PUBLIC_PATH = "/public";
    private static final String EDC_STATE = "'edc:state'";
    public static final String AUTH_CODE_KEY = "authCode";

    @RegisterExtension
    static EdcRuntimeExtension provider = getProvider();

    @RegisterExtension
    static EdcRuntimeExtension consumer = getConsumer();

    @Container
    public static DockerComposeContainer<?> httpRequestLoggerContainer = getHttpRequestLoggerContainer(LOG_CONSUMER);

    @BeforeAll
    static void setUp() {
        httpRequestLoggerContainer.start();
    }

    @Test
    void runSampleSteps() {
        runPrerequisites();
        var contractAgreementId = runNegotiation();
        var transferProcessId = startConsumerPullTransfer(contractAgreementId);
        checkTransferStatus(transferProcessId);
        var authCode = LOG_CONSUMER.getJsonValue(AUTH_CODE_KEY);
        getData(authCode);
    }

    private static String checkTransferStatus(String transferProcessId) {
        return await()
                .atMost(TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .until(
                        () -> get(CONSUMER_MANAGEMENT_HOST + MANAGEMENT_V2_TRANSFER_PROCESSES_PATH + transferProcessId, EDC_STATE),
                        (result) -> TransferProcessStates.STARTED.name().equals(result)
                );
    }

    private static void getData(String authCode) {
        await()
                .atMost(TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .until(
                        () -> get(CONSUMER_PUBLIC_HOST + PUBLIC_PATH, authCode, "[0].name"),
                        "Leanne Graham"::equals
                );
    }
}
