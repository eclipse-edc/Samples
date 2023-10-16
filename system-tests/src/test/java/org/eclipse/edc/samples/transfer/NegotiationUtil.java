package org.eclipse.edc.samples.transfer;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.eclipse.edc.samples.transfer.ConnectorSetupCommon.*;

public class NegotiationUtil {

    private static final String CREATE_ASSET_FILE_PATH = "transfer/transfer-01-negotiation/resources/create-asset.json";
    private static final String MANAGEMENT_V2_ASSETS_PATH = "/management/v2/assets";
    private static final String CREATE_POLICY_FILE_PATH = "transfer/transfer-01-negotiation/resources/create-policy.json";
    private static final String MANAGEMENT_V2_POLICY_DEFINITIONS_PATH = "/management/v2/policydefinitions";
    private static final String CREATE_CONTRACT_DEFINITION_FILE_PATH = "transfer/transfer-01-negotiation/resources/create-contract-definition.json";
    private static final String MANAGEMENT_V2_CONTRACT_DEFINITIONS_PATH = "/management/v2/contractdefinitions";
    private static final String FETCH_CATALOG_FILE_PATH = "transfer/transfer-01-negotiation/resources/fetch-catalog.json";
    private static final String MANAGEMENT_V2_CATALOG_REQUEST_PATH = "/management/v2/catalog/request";
    private static final String NEGOTIATE_CONTRACT_FILE_PATH = "transfer/transfer-01-negotiation/resources/negotiate-contract.json";
    private static final String MANAGEMENT_V2_CONTRACT_NEGOTIATIONS_PATH = "/management/v2/contractnegotiations";

    static void createAsset() {
        assertResponseOk(MANAGEMENT_V2_ASSETS_PATH, CREATE_ASSET_FILE_PATH);
    }

    static void createPolicy() {
        assertResponseOk(MANAGEMENT_V2_POLICY_DEFINITIONS_PATH, CREATE_POLICY_FILE_PATH);
    }

    static void createContractDefinition() {
        assertResponseOk(MANAGEMENT_V2_CONTRACT_DEFINITIONS_PATH, CREATE_CONTRACT_DEFINITION_FILE_PATH);
    }

    static void fetchCatalog() {
        assertResponseOk(MANAGEMENT_V2_CATALOG_REQUEST_PATH, FETCH_CATALOG_FILE_PATH);
    }

    static void negotiateContract() {
        assertResponseOk(MANAGEMENT_V2_CONTRACT_NEGOTIATIONS_PATH, NEGOTIATE_CONTRACT_FILE_PATH);
    }

    private static void assertResponseOk(String urlPath, String filePath) {
        var requestBody = FileTransferCommon.getFileFromRelativePath(filePath);

        given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(PROVIDER_HOST + urlPath)
                .then()
                .log()
                .ifError()
                .statusCode(HttpStatus.SC_OK);
    }
}
