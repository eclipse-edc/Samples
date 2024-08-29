/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Materna Information & Communications SE - initial test implementation for sample
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

public class FileTransferCloudCommon {

    private static final String CONSUMER_MANAGEMENT_URL = "http://localhost:29193/management";
    private static final String V3_CATALOG_DATASET_REQUEST_PATH = "/v3/catalog/dataset/request";
    private static final String FETCH_DATASET_FROM_CATALOG_FILE_PATH = "transfer/transfer-05-file-transfer-cloud/resources/get-dataset.json";
    private static final String CATALOG_DATASET_ID = "\"odrl:hasPolicy\".'@id'";
    private static final String NEGOTIATE_CONTRACT_FILE_PATH = "transfer/transfer-05-file-transfer-cloud/resources/negotiate-contract.json";
    private static final String V3_CONTRACT_NEGOTIATIONS_PATH = "/v3/contractnegotiations/";
    private static final String CONTRACT_NEGOTIATION_ID = "@id";
    private static final String CONTRACT_AGREEMENT_ID = "contractAgreementId";
    private static final String CONTRACT_OFFER_ID_KEY = "{{contract-offer-id}}";

    public static String fetchDatasetFromCatalog(String fetchDatasetFromCatalogFilePath) {
        var catalogDatasetId = post(
                CONSUMER_MANAGEMENT_URL + V3_CATALOG_DATASET_REQUEST_PATH,
                getFileContentFromRelativePath(fetchDatasetFromCatalogFilePath),
                CATALOG_DATASET_ID
        );
        assertThat(catalogDatasetId).isNotEmpty();
        return catalogDatasetId;
    }

    public static String negotiateContract(String negotiateContractFilePath, String catalogDatasetId) {
        var requestBody = getFileContentFromRelativePath(negotiateContractFilePath)
                .replace(CONTRACT_OFFER_ID_KEY, catalogDatasetId);
        var contractNegotiationId = post(
                CONSUMER_MANAGEMENT_URL + V3_CONTRACT_NEGOTIATIONS_PATH,
                requestBody,
                CONTRACT_NEGOTIATION_ID
        );
        assertThat(contractNegotiationId).isNotEmpty();
        return contractNegotiationId;
    }

    public static String getContractAgreementId(String contractNegotiationId) {
        var url = CONSUMER_MANAGEMENT_URL + V3_CONTRACT_NEGOTIATIONS_PATH + contractNegotiationId;
        return await()
                .atMost(TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .until(() -> get(url, CONTRACT_AGREEMENT_ID), Objects::nonNull);
    }

    public static String runNegotiation() {
        var catalogDatasetId = fetchDatasetFromCatalog(FETCH_DATASET_FROM_CATALOG_FILE_PATH);
        var contractNegotiationId = negotiateContract(NEGOTIATE_CONTRACT_FILE_PATH, catalogDatasetId);
        return getContractAgreementId(contractNegotiationId);
    }

}
