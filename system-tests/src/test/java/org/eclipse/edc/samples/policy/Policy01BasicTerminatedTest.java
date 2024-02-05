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


import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.eclipse.edc.junit.testfixtures.TestUtils;
import org.eclipse.edc.samples.util.TransferUtil;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

@EndToEndTest
public class Policy01BasicTerminatedTest {

    static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-01-policy-enforcement/policy-enforcement-provider/config.properties";
    static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "policy/policy-01-policy-enforcement/policy-enforcement-consumer/config-us.properties";
    static final String CONTRACT_OFFER_FILE_PATH = "policy/policy-01-policy-enforcement/contractoffer.json";

    @RegisterExtension
    static EdcRuntimeExtension provider = new EdcRuntimeExtension(":policy:policy-01-policy-enforcement:policy-enforcement-provider", "provider", Map.of("edc.fs.config", getFileFromRelativePath(PROVIDER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()));

    @RegisterExtension
    static EdcRuntimeExtension consumer = new EdcRuntimeExtension(":policy:policy-01-policy-enforcement:policy-enforcement-consumer", "consumer", Map.of("edc.fs.config", getFileFromRelativePath(CONSUMER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()));

    String contractNegotiationId;

    @Test
    void runSampleSteps() {
        initiateContractNegotiation(CONTRACT_OFFER_FILE_PATH);
        lookUpContractAgreementTerminated();
    }

    @NotNull
    public static File getFileFromRelativePath(String relativePath) {
        return new File(TestUtils.findBuildRoot(), relativePath);
    }

    void initiateContractNegotiation(String contractOfferFilePath) {
        File file = new File(TestUtils.findBuildRoot(), contractOfferFilePath);
        String fileContent = readFileContent(file);

        String url = "http://localhost:9192/management/v2/contractnegotiations";
        contractNegotiationId = TransferUtil.post(url, fileContent, "@id");

        if (contractNegotiationId == null || contractNegotiationId.isEmpty()) {
            throw new IllegalStateException("failed to get a valid contract negotiation ID from the response");
        }
    }

    private String readFileContent(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            return content;
        } catch (IOException e) {
            throw new RuntimeException("error reading file content", e);
        }
    }

    void lookUpContractAgreementTerminated() {
        String url = "http://localhost:9192/management/v2/contractnegotiations/" + contractNegotiationId;
        String state = "";
        final long startTime = System.currentTimeMillis();
        final long timeout = 300000;

        do {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }

            state = TransferUtil.get(url, "state");

            if (System.currentTimeMillis() - startTime > timeout) {
                break;
            }

        } while (!"TERMINATED".equals(state));

        assertThat(state, Matchers.equalTo("TERMINATED"));
    }
}
