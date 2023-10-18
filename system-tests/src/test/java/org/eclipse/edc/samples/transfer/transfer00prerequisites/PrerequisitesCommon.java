package org.eclipse.edc.samples.transfer.transfer00prerequisites;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.eclipse.edc.samples.transfer.FileTransferCommon.getFileFromRelativePath;

public class PrerequisitesCommon {
    public static final String API_KEY_HEADER_KEY = "X-Api-Key";
    public static final String API_KEY_HEADER_VALUE = "password";
    public static final String PROVIDER_MANAGEMENT_URL = "http://localhost:19193/management";
    public static final String CONSUMER_MANAGEMENT_URL = "http://localhost:29193/management";
    public static final String CONSUMER_PUBLIC_URL = "http://localhost:29291/public";

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
    private static final String V2_DATAPLANES_PATH = "/v2/dataplanes";

    public static EdcRuntimeExtension getProvider() {
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

    public static EdcRuntimeExtension getConsumer() {
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

    public static void registerDataPlaneProvider() {
        registerDataPlane(PROVIDER_MANAGEMENT_URL, REGISTER_DATA_PLANE_PROVIDER_JSON);
    }

    public static void registerDataPlaneConsumer() {
        registerDataPlane(CONSUMER_MANAGEMENT_URL, REGISTER_DATA_PLANE_CONSUMER_JSON);
    }

    public static void runPrerequisites() {
        registerDataPlaneProvider();
        registerDataPlaneConsumer();
    }

    private static void registerDataPlane(String host, String payloadPath) {
        var requestBody = getFileFromRelativePath(payloadPath);

        given()
                .headers(API_KEY_HEADER_KEY, API_KEY_HEADER_VALUE)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(host + V2_DATAPLANES_PATH)
                .then()
                .log()
                .ifError()
                .statusCode(HttpStatus.SC_OK);
    }
}
