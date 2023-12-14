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

import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.eclipse.edc.samples.policy.BasicPolicySampleTestCommon.getFileFromRelativePath;

//@EndToEndTest
public class Policy01BasicFinalizedTest {

    static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-01-policy-enforcement/policy-enforcement-provider/config.properties";
    static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-01-policy-enforcement/policy-enforcement-consumer/config.properties";
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

    final BasicPolicySampleTestCommon testUtils = new BasicPolicySampleTestCommon();

    @Test
    void runSampleSteps() {
        testUtils.initiateContractNegotiation(CONTRACT_OFFER_FILE_PATH);
        testUtils.lookUpContractAgreementFinalized();
    }
}
