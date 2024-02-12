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

package org.eclipse.edc.samples.common;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.junit.testfixtures.TestUtils.findBuildRoot;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.util.TransferUtil.POLL_INTERVAL;
import static org.eclipse.edc.samples.util.TransferUtil.TIMEOUT;
import static org.eclipse.edc.samples.util.TransferUtil.get;
import static org.eclipse.edc.samples.util.TransferUtil.post;

public class PolicyCommon {
    
    private static final String CONSUMER_NEGOTIATIONS_URL = "http://localhost:9192/management/v2/contractnegotiations/";
    private static final String CONTRACT_OFFER_FILE_PATH = "policy/policy-01-policy-enforcement/resources/contractoffer.json";
    
    @NotNull
    public static File getFileFromRelativePath(String relativePath) {
        return new File(findBuildRoot(), relativePath);
    }
    
    public static String initiateContractNegotiation() {
        var requestBody = getFileContentFromRelativePath(CONTRACT_OFFER_FILE_PATH);
        
        var negotiationId = post(CONSUMER_NEGOTIATIONS_URL, requestBody, "@id");
        
        assertThat(negotiationId)
                .isNotNull()
                .isNotEmpty();
        
        return negotiationId;
    }
    
    public static void lookUpContractAgreementState(String negotiationId, String expectedState) {
        var url = CONSUMER_NEGOTIATIONS_URL + negotiationId;
        
        await()
                .atMost(TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .until(() -> get(url, "state"), state -> state.equals(expectedState));
    }
    
}
