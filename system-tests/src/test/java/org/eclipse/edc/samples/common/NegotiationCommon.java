/*
 *  Copyright (c) 2023 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Initial implementation
 *
 */

package org.eclipse.edc.samples.common;


import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.util.TransferUtil.POLL_INTERVAL;
import static org.eclipse.edc.samples.util.TransferUtil.TIMEOUT;
import static org.eclipse.edc.samples.util.TransferUtil.get;
import static org.eclipse.edc.samples.util.TransferUtil.post;

public class NegotiationCommon {

    private static final String CREATE_ASSET_FILE_PATH = "transfer/transfer-01-negotiation/resources/create-asset.json";
    private static final String V3_ASSETS_PATH = "/v3/assets";
    private static final String CREATE_POLICY_FILE_PATH = "transfer/transfer-01-negotiation/resources/create-policy.json";
    private static final String V2_POLICY_DEFINITIONS_PATH = "/v2/policydefinitions";
    private static final String CREATE_CONTRACT_DEFINITION_FILE_PATH = "transfer/transfer-01-negotiation/resources/create-contract-definition.json";
    private static final String V2_CONTRACT_DEFINITIONS_PATH = "/v2/contractdefinitions";
    private static final String FETCH_CATALOG_FILE_PATH = "transfer/transfer-01-negotiation/resources/fetch-catalog.json";
    private static final String V2_CATALOG_REQUEST_PATH = "/v2/catalog/request";
    private static final String NEGOTIATE_CONTRACT_FILE_PATH = "transfer/transfer-01-negotiation/resources/negotiate-contract.json";
    private static final String V2_CONTRACT_NEGOTIATIONS_PATH = "/v2/contractnegotiations/";
    private static final String CONTRACT_NEGOTIATION_ID = "@id";
    private static final String CONTRACT_AGREEMENT_ID = "contractAgreementId";

    public static void createAsset() {
        post(PrerequisitesCommon.PROVIDER_MANAGEMENT_URL + V3_ASSETS_PATH, getFileContentFromRelativePath(CREATE_ASSET_FILE_PATH));
    }

    public static void createPolicy() {
        post(PrerequisitesCommon.PROVIDER_MANAGEMENT_URL + V2_POLICY_DEFINITIONS_PATH, getFileContentFromRelativePath(CREATE_POLICY_FILE_PATH));
    }

    public static void createContractDefinition() {
        post(PrerequisitesCommon.PROVIDER_MANAGEMENT_URL + V2_CONTRACT_DEFINITIONS_PATH, getFileContentFromRelativePath(CREATE_CONTRACT_DEFINITION_FILE_PATH));
    }

    public static void fetchCatalog() {
        post(PrerequisitesCommon.CONSUMER_MANAGEMENT_URL + V2_CATALOG_REQUEST_PATH, getFileContentFromRelativePath(FETCH_CATALOG_FILE_PATH));
    }

    public static String negotiateContract(String negotiateContractFilePath) {
        var contractNegotiationId = post(PrerequisitesCommon.CONSUMER_MANAGEMENT_URL + V2_CONTRACT_NEGOTIATIONS_PATH, getFileContentFromRelativePath(negotiateContractFilePath), CONTRACT_NEGOTIATION_ID);
        assertThat(contractNegotiationId).isNotEmpty();
        return contractNegotiationId;
    }

    public static String negotiateContract() {
        return negotiateContract(NEGOTIATE_CONTRACT_FILE_PATH);
    }

    public static String getContractAgreementId(String contractNegotiationId) {
        String url = PrerequisitesCommon.CONSUMER_MANAGEMENT_URL + V2_CONTRACT_NEGOTIATIONS_PATH + contractNegotiationId;
        return await()
                .atMost(TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .until(() -> get(url, CONTRACT_AGREEMENT_ID), Objects::nonNull);
    }

    public static String runNegotiation() {
        createAsset();
        createPolicy();
        createContractDefinition();
        fetchCatalog();
        var contractNegotiationId = negotiateContract();
        return getContractAgreementId(contractNegotiationId);
    }
}
