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

package org.eclipse.edc.sample.extension;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.eclipse.edc.sample.extension.PolicyProvisionSampleTestCommon.getFileFromRelativePath;

/**
 * This test class runs test for checking if the policy defined in {@code provision.menifest.verify} scope is regulating the destination location properly.
 * We will be providing a destination file path, as we have used in previous samples for file transfers.
 * However, because of our policy, the file will be stored in the desired location (NOT in the provided destination file path) that is defined in the policy.
 */
@EndToEndTest
public class PolicyProvisionSampleTest {
    static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-02-provision/policy-provision-consumer/config.properties";
    static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-02-provision/policy-provision-provider/config.properties";
    static final String SAMPLE_ASSET_FILE_PATH = "policy/policy-02-provision/README.md";
    /**
     * {@link PolicyProvisionSampleTest#DESTINATION_FILE_PATH} file path that will be used as destination path in
     * DataRequest during initiate transfer request
     */
    static final String DESTINATION_FILE_PATH = "policy/policy-02-provision/requested_file.txt";
    /**
     * {@link PolicyProvisionSampleTest#DESIRED_DESTINATION_FILE_PATH} file path that will be defined in our policy as the destination.
     * In expected behavior the file will be stored in this location
     */
    static final String DESIRED_DESTINATION_FILE_PATH = "policy/policy-02-provision/desired_requested_file.txt";
    static final String TRANSFER_FILE_PATH = "policy/policy-02-provision/filetransfer.json";
    static final String CONTRACT_OFFER_FILE_PATH = "policy/policy-02-provision/contractoffer.json";

    @RegisterExtension
    static EdcRuntimeExtension provider = new EdcRuntimeExtension(
            ":policy:policy-02-provision:policy-provision-provider",
            "provider",
            Map.of(
                    // Override 'edc.samples.transfer.01.asset.path' implicitly set via property 'edc.fs.config'.
                    "edc.samples.policy-02.asset.path", getFileFromRelativePath(SAMPLE_ASSET_FILE_PATH).getAbsolutePath(),
                    "edc.samples.policy-02.constraint.desired.file.path", getFileFromRelativePath(DESIRED_DESTINATION_FILE_PATH).getAbsolutePath(),
                    "edc.fs.config", getFileFromRelativePath(PROVIDER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
            )
    );
    @RegisterExtension
    static EdcRuntimeExtension consumer = new EdcRuntimeExtension(
            ":policy:policy-02-provision:policy-provision-consumer",
            "consumer",
            Map.of(
                    "edc.fs.config", getFileFromRelativePath(CONSUMER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
            )
    );
    final PolicyProvisionSampleTestCommon testUtils = new PolicyProvisionSampleTestCommon(SAMPLE_ASSET_FILE_PATH, DESTINATION_FILE_PATH, DESIRED_DESTINATION_FILE_PATH);

    /**
     * Run all sample steps in one single test.
     * Note: Sample steps cannot be separated into single tests because {@link EdcRuntimeExtension}
     * runs before each single test.
     */
    @Test
    void runSampleSteps() throws Exception {
        testUtils.assertTestPrerequisites();

        testUtils.initiateContractNegotiation(CONTRACT_OFFER_FILE_PATH);
        testUtils.lookUpContractAgreementId();
        var transferProcessId = testUtils.requestTransferFile(TRANSFER_FILE_PATH);
        // this will check if the desired file in location DESIRED_DESTINATION_FILE_PATH contains the same content as the sample
        testUtils.assertDestinationFileContent();
        // this will assert that no file has been created in the location DESTINATION_FILE_PATH that was used for initiate transfer request
        testUtils.assertFileDoesNotExist();
        testUtils.assertTransferProcessStatusConsumerSide(transferProcessId);
    }

    @AfterEach
    protected void tearDown() {
        testUtils.cleanTemporaryTestFiles();
    }
}