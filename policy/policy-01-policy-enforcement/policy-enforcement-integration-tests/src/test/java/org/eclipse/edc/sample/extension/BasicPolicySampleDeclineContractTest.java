/*
 *  Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
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

package org.eclipse.edc.sample.extension;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.eclipse.edc.sample.extension.BasicPolicySampleTestCommon.getFileFromRelativePath;


/**
 * This test class runs test for the scenario when the contract negotiation gets DECLINED
 * It starts a connector with config.properties, containing a start and end date for which the current time is NOT within the defined interval.
 */
@EndToEndTest
public class BasicPolicySampleDeclineContractTest {
    static final String CONNECTOR_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-01-contract-negotiation/policy-contract-negotiation-connector/config.properties";
    static final String CONTRACT_OFFER_FILE_PATH = "policy/policy-01-contract-negotiation/policy-contract-negotiation-integration-tests/src/testFixtures/resources/contractoffer.json";

    @RegisterExtension
    static EdcRuntimeExtension connector = new EdcRuntimeExtension(
            ":policy:policy-01-contract-negotiation:policy-contract-negotiation-connector",
            "connector",
            Map.of(
                    // Override 'edc.samples.policy-01.constraint.date.start' & 'edc.samples.policy-01.constraint.date.end' implicitly set via property 'edc.fs.config'.
                    "edc.samples.policy-01.constraint.date.start", "2022-01-01T00:00:00.000+02:00",
                    "edc.samples.policy-01.constraint.date.end", "2022-12-31T00:00:00.000+02:00",
                    "edc.fs.config", getFileFromRelativePath(CONNECTOR_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
            )
    );

    final BasicPolicySampleTestCommon testUtils = new BasicPolicySampleTestCommon();

    /**
     * Run all sample steps in one single test.
     * Note: Sample steps cannot be separated into single tests because {@link EdcRuntimeExtension}
     * runs before each single test.
     */
    @Test
    void runSampleSteps() {
        testUtils.initiateContractNegotiation(CONTRACT_OFFER_FILE_PATH);
        testUtils.lookUpContractAgreementDeclined();
    }
}