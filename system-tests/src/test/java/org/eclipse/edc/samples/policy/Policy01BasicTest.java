/*
 *  Copyright (c) 2024 Fraunhofer Institute for Software and Systems Engineering
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

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.eclipse.edc.samples.common.NegotiationCommon;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;
import static org.eclipse.edc.samples.common.NegotiationCommon.getContractNegotiationState;
import static org.eclipse.edc.samples.common.NegotiationCommon.negotiateContract;
import static org.eclipse.edc.samples.common.PolicyCommon.createAsset;
import static org.eclipse.edc.samples.common.PolicyCommon.createContractDefinition;
import static org.eclipse.edc.samples.common.PolicyCommon.createPolicy;
import static org.eclipse.edc.samples.util.TransferUtil.POLL_INTERVAL;
import static org.eclipse.edc.samples.util.TransferUtil.TIMEOUT;

@EndToEndTest
class Policy01BasicTest {

    private static final String SAMPLE_FOLDER = "policy/policy-01-policy-enforcement";
    private static final String CREATE_ASSET_FILE_PATH = SAMPLE_FOLDER + "/resources/create-asset.json";
    private static final String CREATE_POLICY_FILE_PATH = SAMPLE_FOLDER + "/resources/create-policy.json";
    private static final String CREATE_CONTRACT_DEFINITION_FILE_PATH = SAMPLE_FOLDER + "/resources/create-contract-definition.json";
    private static final String CONTRACT_OFFER_FILE_PATH = SAMPLE_FOLDER + "/resources/contract-request.json";

    @RegisterExtension
    static final EdcRuntimeExtension PROVIDER_RUNTIME = provider();

    @Nested
    class Terminated {

        @RegisterExtension
        static final EdcRuntimeExtension CONSUMER_RUNTIME = consumer("system-tests/src/test/resources/policy/config-us.properties");

        @Test
        void runSampleSteps() {
            createAsset(CREATE_ASSET_FILE_PATH);
            createPolicy(CREATE_POLICY_FILE_PATH);
            createContractDefinition(CREATE_CONTRACT_DEFINITION_FILE_PATH);
            var negotiationId = negotiateContract(CONTRACT_OFFER_FILE_PATH, "");

            await()
                    .atMost(TIMEOUT)
                    .pollInterval(POLL_INTERVAL)
                    .until(() -> NegotiationCommon.getContractNegotiationState(negotiationId), s -> s.equals("TERMINATED"));
        }

    }

    @Nested
    class Finalized {

        @RegisterExtension
        static final EdcRuntimeExtension CONSUMER_RUNTIME = consumer("system-tests/src/test/resources/policy/config-eu.properties");

        @Test
        void runSampleSteps() {
            createAsset(CREATE_ASSET_FILE_PATH);
            createPolicy(CREATE_POLICY_FILE_PATH);
            createContractDefinition(CREATE_CONTRACT_DEFINITION_FILE_PATH);
            var negotiationId = negotiateContract(CONTRACT_OFFER_FILE_PATH, "");

            await().atMost(TIMEOUT).pollInterval(POLL_INTERVAL)
                    .until(() -> getContractNegotiationState(negotiationId), s -> s.equals("FINALIZED"));
        }

    }

    private static EdcRuntimeExtension provider() {
        return new EdcRuntimeExtension(":policy:policy-01-policy-enforcement:policy-enforcement-provider",
                "provider",
                Map.of("edc.fs.config", getFileFromRelativePath(SAMPLE_FOLDER + "/policy-enforcement-provider/config.properties").getAbsolutePath())
        );
    }

    private static EdcRuntimeExtension consumer(String configurationFilePath) {
        return new EdcRuntimeExtension(":policy:policy-01-policy-enforcement:policy-enforcement-consumer",
                "consumer", Map.of("edc.fs.config", getFileFromRelativePath(configurationFilePath).getAbsolutePath()));
    }

}
