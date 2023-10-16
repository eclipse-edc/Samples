package org.eclipse.edc.samples.transfer;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.eclipse.edc.samples.transfer.FileTransferCommon.getFileFromRelativePath;

public class ConnectorSetupCommon {
    static final String API_KEY_HEADER_KEY = "X-Api-Key";
    static final String API_KEY_HEADER_VALUE = "password";
    static final String PROVIDER_HOST = "http://localhost:19193";
    static final String CONSUMER_HOST = "http://localhost:29193";

    private static final String CONNECTOR_MODULE_PATH = ":transfer:transfer-00-prerequisites:connector";
    private static final String PROVIDER = "provider";
    private static final String CONSUMER = "consumer";
    private static final String EDC_KEYSTORE = "edc.keystore";
    private static final String EDC_KEYSTORE_PASSWORD = "edc.keystore.password";
    private static final String EDC_VAULT = "edc.vault";
    private static final String EDC_FS_CONFIG = "edc.fs.config";

    private static final String CERT_PFX_FILE_PATH = "transfer/transfer-00-prerequisites/resources/certs/cert.pfx";
    private static final String KEYSTORE_PASSWORD = "123456";
    private static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-00-prerequisites/resources/configuration/provider-configuration.properties";
    private static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-00-prerequisites/resources/configuration/consumer-configuration.properties";
    private static final String PROVIDER_VAULT_PROPERTIES_FILE_PATH = "transfer/transfer-00-prerequisites/resources/configuration/provider-vault.properties";
    private static final String CONSUMER_VAULT_PROPERTIES_FILE_PATH = "transfer/transfer-00-prerequisites/resources/configuration/consumer-vault.properties";
    private static final String REGISTER_DATA_PLANE_PROVIDER_JSON = "transfer/transfer-00-prerequisites/resources/dataplane/register-data-plane-provider.json";
    private static final String REGISTER_DATA_PLANE_CONSUMER_JSON = "transfer/transfer-00-prerequisites/resources/dataplane/register-data-plane-consumer.json";
    private static final String MANAGEMENT_V2_DATAPLANES_PATH = "/management/v2/dataplanes";

    static EdcRuntimeExtension getProvider() {
        return new EdcRuntimeExtension(
                CONNECTOR_MODULE_PATH,
                PROVIDER,
                Map.of(
                        EDC_KEYSTORE, getFileFromRelativePath(CERT_PFX_FILE_PATH).getAbsolutePath(),
                        EDC_KEYSTORE_PASSWORD, KEYSTORE_PASSWORD,
                        EDC_VAULT, getFileFromRelativePath(PROVIDER_VAULT_PROPERTIES_FILE_PATH).getAbsolutePath(),
                        EDC_FS_CONFIG, getFileFromRelativePath(PROVIDER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
                )
        );
    }

    static EdcRuntimeExtension getConsumer() {
        return new EdcRuntimeExtension(
                CONNECTOR_MODULE_PATH,
                CONSUMER,
                Map.of(
                        EDC_KEYSTORE, getFileFromRelativePath(CERT_PFX_FILE_PATH).getAbsolutePath(),
                        EDC_KEYSTORE_PASSWORD, KEYSTORE_PASSWORD,
                        EDC_VAULT, getFileFromRelativePath(CONSUMER_VAULT_PROPERTIES_FILE_PATH).getAbsolutePath(),
                        EDC_FS_CONFIG, getFileFromRelativePath(CONSUMER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
                )
        );
    }

    static void registerDataPlaneProvider() {
        registerDataPlane(PROVIDER_HOST, REGISTER_DATA_PLANE_PROVIDER_JSON);
    }

    static void registerDataPlaneConsumer() {
        registerDataPlane(CONSUMER_HOST, REGISTER_DATA_PLANE_CONSUMER_JSON);
    }

    private static void registerDataPlane(String host, String payloadPath) {
        var requestBody = FileTransferCommon.getFileFromRelativePath(payloadPath);

        given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(host + MANAGEMENT_V2_DATAPLANES_PATH)
                .then()
                .log()
                .ifError()
                .statusCode(HttpStatus.SC_OK);
    }
}
