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

package org.eclipse.edc.samples.common;

import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.util.TransferUtil.post;

public class PolicyCommon {
    
    private static final String CREATE_ASSET_FILE_PATH = "policy/policy-01-policy-enforcement/resources/create-asset.json";
    private static final String V3_ASSETS_PATH = "/v3/assets";
    private static final String CREATE_POLICY_FILE_PATH = "policy/policy-01-policy-enforcement/resources/create-policy.json";
    private static final String V2_POLICY_DEFINITIONS_PATH = "/v2/policydefinitions";
    private static final String CREATE_CONTRACT_DEFINITION_FILE_PATH = "policy/policy-01-policy-enforcement/resources/create-contract-definition.json";
    private static final String V2_CONTRACT_DEFINITIONS_PATH = "/v2/contractdefinitions";
    
    public static void createAsset() {
        post(PrerequisitesCommon.PROVIDER_MANAGEMENT_URL + V3_ASSETS_PATH, getFileContentFromRelativePath(CREATE_ASSET_FILE_PATH));
    }
    
    public static void createPolicy() {
        post(PrerequisitesCommon.PROVIDER_MANAGEMENT_URL + V2_POLICY_DEFINITIONS_PATH, getFileContentFromRelativePath(CREATE_POLICY_FILE_PATH));
    }
    
    public static void createContractDefinition() {
        post(PrerequisitesCommon.PROVIDER_MANAGEMENT_URL + V2_CONTRACT_DEFINITIONS_PATH, getFileContentFromRelativePath(CREATE_CONTRACT_DEFINITION_FILE_PATH));
    }
    
}
