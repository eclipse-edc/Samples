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
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.eclipse.edc.samples.common.NegotiationCommon;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.common.NegotiationCommon.getContractNegotiationState;
import static org.eclipse.edc.samples.common.NegotiationCommon.negotiateContract;
import static org.eclipse.edc.samples.common.PolicyCommon.createAsset;
import static org.eclipse.edc.samples.common.PolicyCommon.createContractDefinition;
import static org.eclipse.edc.samples.common.PolicyCommon.createPolicy;
import static org.eclipse.edc.samples.util.ConfigPropertiesLoader.fromPropertiesFile;
import static org.eclipse.edc.samples.util.TransferUtil.POLL_INTERVAL;

@EndToEndTest
class Policy01BasicTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(90);
    private static final String SAMPLE_FOLDER = "policy/policy-01-policy-enforcement";
    private static final String CREATE_ASSET_FILE_PATH = SAMPLE_FOLDER + "/resources/create-asset.json";
    private static final String CREATE_POLICY_FILE_PATH = SAMPLE_FOLDER + "/resources/create-policy.json";
    private static final String CREATE_CONTRACT_DEFINITION_FILE_PATH = SAMPLE_FOLDER + "/resources/create-contract-definition.json";
    private static final String CONTRACT_OFFER_FILE_PATH = SAMPLE_FOLDER + "/resources/contract-request.json";

    @Nested
    class Terminated {

        @RegisterExtension
        static final RuntimeExtension PROVIDER_RUNTIME = provider();

        @RegisterExtension
        static final RuntimeExtension CONSUMER_RUNTIME = consumer("system-tests/src/test/resources/policy/config-us.properties");

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
        static final RuntimeExtension PROVIDER_RUNTIME = provider();

        @RegisterExtension
        static final RuntimeExtension CONSUMER_RUNTIME = consumer("system-tests/src/test/resources/policy/config-eu.properties");

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

    private static RuntimeExtension provider() {
        return new RuntimePerClassExtension(new EmbeddedRuntime(
                "provider",
                ":policy:policy-01-policy-enforcement:policy-enforcement-provider"
        ).configurationProvider(fromPropertiesFile(SAMPLE_FOLDER + "/policy-enforcement-provider/config.properties")));
    }

    private static RuntimeExtension consumer(String configurationFilePath) {
        return new RuntimePerClassExtension(new EmbeddedRuntime(
                "consumer",
                ":policy:policy-01-policy-enforcement:policy-enforcement-consumer"
        ).configurationProvider(fromPropertiesFile(configurationFilePath)));
    }

}
