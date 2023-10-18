package org.eclipse.edc.samples.transfer.transfer01negotiation;


import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.transfer.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.transfer.TransferUtil.*;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.CONSUMER_MANAGEMENT_URL;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.PROVIDER_MANAGEMENT_URL;

public class NegotiationCommon {

    private static final String CREATE_ASSET_FILE_PATH = "transfer/transfer-01-negotiation/resources/create-asset.json";
    private static final String V2_ASSETS_PATH = "/v2/assets";
    private static final String CREATE_POLICY_FILE_PATH = "transfer/transfer-01-negotiation/resources/create-policy.json";
    private static final String V2_POLICY_DEFINITIONS_PATH = "/v2/policydefinitions";
    private static final String CREATE_CONTRACT_DEFINITION_FILE_PATH = "transfer/transfer-01-negotiation/resources/create-contract-definition.json";
    private static final String V2_CONTRACT_DEFINITIONS_PATH = "/v2/contractdefinitions";
    private static final String FETCH_CATALOG_FILE_PATH = "transfer/transfer-01-negotiation/resources/fetch-catalog.json";
    private static final String V2_CATALOG_REQUEST_PATH = "/v2/catalog/request";
    private static final String NEGOTIATE_CONTRACT_FILE_PATH = "transfer/transfer-01-negotiation/resources/negotiate-contract.json";
    private static final String V2_CONTRACT_NEGOTIATIONS_PATH = "/v2/contractnegotiations/";
    private static final String CONTRACT_NEGOTIATION_ID = "@id";
    private static final String CONTRACT_AGREEMENT_ID = "'edc:contractAgreementId'";

    public static void createAsset() {
        post(PROVIDER_MANAGEMENT_URL + V2_ASSETS_PATH, getFileContentFromRelativePath(CREATE_ASSET_FILE_PATH));
    }

    public static void createPolicy() {
        post(PROVIDER_MANAGEMENT_URL + V2_POLICY_DEFINITIONS_PATH, getFileContentFromRelativePath(CREATE_POLICY_FILE_PATH));
    }

    public static void createContractDefinition() {
        post(PROVIDER_MANAGEMENT_URL + V2_CONTRACT_DEFINITIONS_PATH, getFileContentFromRelativePath(CREATE_CONTRACT_DEFINITION_FILE_PATH));
    }

    public static void fetchCatalog() {
        post(CONSUMER_MANAGEMENT_URL + V2_CATALOG_REQUEST_PATH, getFileContentFromRelativePath(FETCH_CATALOG_FILE_PATH));
    }

    public static String negotiateContract() {
        var contractNegotiationId = post(CONSUMER_MANAGEMENT_URL + V2_CONTRACT_NEGOTIATIONS_PATH, getFileContentFromRelativePath(NEGOTIATE_CONTRACT_FILE_PATH), CONTRACT_NEGOTIATION_ID);
        assertThat(contractNegotiationId).isNotEmpty();
        return contractNegotiationId;
    }

    public static String getContractAgreementId(String contractNegotiationId) {
        return await()
                .atMost(TIMEOUT)
                .pollInterval(POLL_INTERVAL)
                .until(() -> get(CONSUMER_MANAGEMENT_URL + V2_CONTRACT_NEGOTIATIONS_PATH + contractNegotiationId, CONTRACT_AGREEMENT_ID), Objects::nonNull);
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
