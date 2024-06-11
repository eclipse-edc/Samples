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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Map;

import static org.eclipse.edc.samples.policy.PolicyProvisionSampleTestCommon.getFileFromRelativePath;

/**
 * This test class runs test for checking if the policy defined in {@code provision.manifest.verify} scope is regulating the destination location properly.
 * We will be providing a destination file path, as we have used in previous samples for file transfers.
 * However, because of our policy, the file will be stored in the desired location (NOT in the provided destination file path) that is defined in the policy.
 */
//@EndToEndTest
public class PolicyProvisionSampleTest {
    static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-02-provision/policy-provision-consumer/config.properties";
    static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-02-provision/policy-provision-provider/config.properties";
    static final String SAMPLE_ASSET_FILE_PATH = "policy/policy-02-provision/README.md";
    static final String DESTINATION_FILE_PATH = "policy/policy-02-provision/requested_file.txt";
    static final String DESIRED_DESTINATION_FILE_PATH = "policy/policy-02-provision/transfer.txt";
    static final String TRANSFER_FILE_PATH = "policy/policy-02-provision/filetransfer.json";
    static final String CONTRACT_OFFER_FILE_PATH = "policy/policy-02-provision/contractoffer.json";

    @RegisterExtension
    static EdcRuntimeExtension provider = new EdcRuntimeExtension(
            ":policy:policy-02-provision:policy-provision-provider",
            "provider",
            Map.of(
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

    @Test
    void runSampleSteps() throws Exception {
        testUtils.assertTestPrerequisites();

        testUtils.initiateContractNegotiation();
        testUtils.lookUpContractAgreementId();
        var transferProcessId = testUtils.requestTransferFile();
        testUtils.assertDestinationFileContent();
        testUtils.assertFileDoesNotExist();
        testUtils.assertTransferProcessStatusConsumerSide(transferProcessId);
    }

    @AfterEach
    protected void tearDown() {
        testUtils.cleanTemporaryTestFiles();
    }
}
