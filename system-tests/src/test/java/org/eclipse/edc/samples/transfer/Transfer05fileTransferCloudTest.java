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

package org.eclipse.edc.samples.transfer;

import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.vault.VaultContainer;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.eclipse.edc.samples.common.FileTransferCloudCommon.runNegotiation;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;
import static org.eclipse.edc.samples.util.TransferUtil.checkTransferStatus;
import static org.eclipse.edc.samples.util.TransferUtil.startTransfer;

@Testcontainers
@EndToEndTest
public class Transfer05fileTransferCloudTest {

    private static final String EDC_FS_CONFIG = "edc.fs.config";

    private static final String CLOUD_CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-05-file-transfer-cloud/cloud-transfer-consumer/config.properties";
    private static final String START_TRANSFER_FILE_PATH = "transfer/transfer-05-file-transfer-cloud/resources/start-transfer.json";

    private static final String PROVIDER = "provider";
    private static final String CONSUMER = "consumer";

    private static final String PROVIDER_MODULE_PATH = ":transfer:transfer-05-file-transfer-cloud:cloud-transfer-provider";
    private static final String CONSUMER_MODULE_PATH = ":transfer:transfer-05-file-transfer-cloud:cloud-transfer-consumer";

    private static final String AZURITE_IMAGE_NAME = "mcr.microsoft.com/azure-storage/azurite:latest";
    private static final String AZURITE_ACCOUNT_NAME = "provider";
    private static final String AZURITE_ACCOUNT_KEY = "password";
    private static final String AZURITE_CONTAINER_NAME = "src-container";
    private static final int AZURITE_PORT = 10000;

    private static final String FILE_NAME = "test-document.txt";

    private static final String MINIO_IMAGE_NAME = "minio/minio:latest";
    private static final String MINIO_ACCOUNT_NAME = "consumer";
    private static final String MINIO_ACCOUNT_KEY = "password";
    private static final String MINIO_BUCKET_NAME = "src-bucket";
    private static final int MINIO_PORT = 9000;

    private static final String VAULT_IMAGE_NAME = "hashicorp/vault:latest";
    private static final String VAULT_TOKEN = "<root-token>"; 
    private static final int VAULT_PORT = 8200;


    @AfterAll
    static void tearDown() {

        if (vaultContainer != null) {
            vaultContainer.stop();
        }
        if (azuriteContainer != null) {
            azuriteContainer.stop();
        }
        if (minioContainer != null) {
            minioContainer.stop();
        }

    }

    @Container
    protected static VaultContainer<?> vaultContainer = new VaultContainer<>(DockerImageName.parse(VAULT_IMAGE_NAME))
            .withExposedPorts(VAULT_PORT)
            .withVaultToken(VAULT_TOKEN)
            .withInitCommand(
                    "kv put secret/accessKeyId content=" + MINIO_ACCOUNT_NAME,
                    "kv put secret/secretAccessKey content=" + MINIO_ACCOUNT_KEY,
                    "kv put secret/provider-key content=" + AZURITE_ACCOUNT_KEY
            )
            .withLogConsumer((OutputFrame outputFrame) -> System.out.print(outputFrame.getUtf8String()));
        
    @Container
    protected static MinIOContainer minioContainer = new MinIOContainer(DockerImageName.parse(MINIO_IMAGE_NAME))
            .withEnv("MINIO_ROOT_USER", MINIO_ACCOUNT_NAME)
            .withEnv("MINIO_ROOT_PASSWORD", MINIO_ACCOUNT_KEY)
            .withExposedPorts(MINIO_PORT)
            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));
   
    @Container
    protected static GenericContainer<?> azuriteContainer = new GenericContainer<>(DockerImageName.parse(AZURITE_IMAGE_NAME))
            .withExposedPorts(AZURITE_PORT)
            .withEnv("AZURITE_ACCOUNTS", AZURITE_ACCOUNT_NAME + ":" + AZURITE_ACCOUNT_KEY)
            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @RegisterExtension
    protected static RuntimeExtension consumer = new RuntimePerClassExtension(new EmbeddedRuntime(
             CONSUMER,
             Map.of(
                     EDC_FS_CONFIG, getFileFromRelativePath(CLOUD_CONSUMER_CONFIG_PROPERTIES_FILE_PATH).getAbsolutePath()
             ),
             CONSUMER_MODULE_PATH
    ));

    @RegisterExtension
    protected static RuntimeExtension provider = new RuntimePerClassExtension(new EmbeddedRuntime(
            PROVIDER,
            Map.ofEntries(
                    Map.entry("edc.participant.id", "provider"),
                    Map.entry("edc.dsp.callback.address", "http://localhost:19194/protocol"),
                    Map.entry("web.http.port", "19191"),
                    Map.entry("web.http.path", "/api"),
                    Map.entry("web.http.management.port", "19193"),
                    Map.entry("web.http.management.path", "/management"),
                    Map.entry("web.http.protocol.port", "19194"),
                    Map.entry("web.http.protocol.path", "/protocol"),
                    Map.entry("edc.api.auth.key", "password"),
                    Map.entry("edc.transfer.proxy.token.signer.privatekey.alias", "private-key"),
                    Map.entry("edc.transfer.proxy.token.verifier.publickey.alias", "public-key"),
                    Map.entry("web.http.public.port", "19291"),
                    Map.entry("web.http.public.path", "/public"),
                    Map.entry("web.http.control.port", "19192"),
                    Map.entry("web.http.control.path", "/control"),
                    Map.entry("edc.vault.hashicorp.url", "http://127.0.0.1:" + getVaultPort()),
                    Map.entry("edc.vault.hashicorp.token", "<root-token>"),
                    Map.entry("edc.vault.hashicorp.api.secret.path", "/v1/secret"),
                    Map.entry("edc.vault.hashicorp.health.check.enabled", "false"),
                    Map.entry("edc.blobstore.endpoint.template", "http://127.0.0.1:" + getAzuritePort() + "/%s"),
                    Map.entry("edc.aws.access.key", "accessKeyId"),
                    Map.entry("edc.aws.secret.access.key", "secretAccessKey")
            ),
            PROVIDER_MODULE_PATH
    ));

    @Test
    void pushFile() throws Exception {

        var minioClient =
                MinioClient.builder()
                .endpoint("http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(MINIO_PORT))
                .credentials(MINIO_ACCOUNT_NAME, MINIO_ACCOUNT_KEY)
                .build();

        minioClient.makeBucket(MakeBucketArgs.builder().bucket(MINIO_BUCKET_NAME).build());

        var requestBody = getFileContentFromRelativePath(START_TRANSFER_FILE_PATH)
                .replace("http://localhost:9000", "http://localhost:" + minioContainer.getMappedPort(9000).toString());

        var contractAgreementId = runNegotiation();
        
        var transferProcessId = startTransfer(requestBody, contractAgreementId);

        checkTransferStatus(transferProcessId, TransferProcessStates.COMPLETED);

        var objects = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(MINIO_BUCKET_NAME).build());

        assertThat(objects)
                .isNotEmpty().first()
                .extracting(result -> {
                    try {
                        return result.get();
                    } catch (Exception e) {
                        return fail();
                    }
                })
                .satisfies(item -> assertThat(item.objectName()).isEqualTo(FILE_NAME));
    }

    private static void configureAzurite() {

        var blobServiceUrl = String.format("http://%s:%d/%s",
                azuriteContainer.getHost(),
                azuriteContainer.getMappedPort(AZURITE_PORT),
                AZURITE_ACCOUNT_NAME);

        var credential = new StorageSharedKeyCredential(AZURITE_ACCOUNT_NAME, AZURITE_ACCOUNT_KEY);

        var blobContainerClient = new BlobContainerClientBuilder()
                .endpoint(blobServiceUrl)
                .credential(credential)
                .containerName(AZURITE_CONTAINER_NAME)
                .buildClient();
        
        blobContainerClient.create();

        var blobClient = blobContainerClient.getBlobClient(FILE_NAME);
        var blobContent = "Test";

        blobClient.upload(new ByteArrayInputStream(blobContent.getBytes(StandardCharsets.UTF_8)), blobContent.length());

    }

    private static int getAzuritePort() {

        if (!azuriteContainer.isRunning()) {
            azuriteContainer.start();
        }
        configureAzurite();

        return azuriteContainer.getMappedPort(AZURITE_PORT);
    }

    private static int getVaultPort() {

        if (!vaultContainer.isRunning()) {
            vaultContainer.start();
        }

        return vaultContainer.getMappedPort(VAULT_PORT);
    }

}
