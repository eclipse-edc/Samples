/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial test implementation for sample
 *
 */

package org.eclipse.edc.samples.transfer.streaming;

import okhttp3.mockwebserver.MockWebServer;
import org.eclipse.edc.connector.controlplane.test.system.utils.LazySupplier;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.eclipse.edc.util.io.Ports;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates.STARTED;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.util.ConfigPropertiesLoader.fromPropertiesFile;

@EndToEndTest
public class Streaming01httpToHttpTest {

    private static final String SAMPLE_FOLDER = "transfer/streaming/streaming-01-http-to-http";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private static final StreamingParticipant PROVIDER = StreamingParticipant.Builder.newStreamingInstance()
            .name("provider")
            .id("provider")
            .controlPlaneManagement(new LazySupplier<>(() -> URI.create("http://localhost:18181/management")))
            .controlPlaneProtocol(new LazySupplier<>(() -> URI.create("http://localhost:18182/protocol")))
            .build();

    private static final StreamingParticipant CONSUMER = StreamingParticipant.Builder.newStreamingInstance()
            .name("consumer")
            .id("consumer")
            .controlPlaneManagement(new LazySupplier<>(() -> URI.create("http://localhost:28181/management")))
            .controlPlaneProtocol(new LazySupplier<>(() -> URI.create("http://localhost:28182/protocol")))
            .build();

    @RegisterExtension
    static RuntimeExtension providerConnector = new RuntimePerClassExtension(new EmbeddedRuntime(
            "provider", ":transfer:streaming:streaming-01-http-to-http:streaming-01-runtime"
    ).configurationProvider(fromPropertiesFile(SAMPLE_FOLDER + "/streaming-01-runtime/provider.properties")));

    @RegisterExtension
    static RuntimeExtension consumerConnector =  new RuntimePerClassExtension(new EmbeddedRuntime(
            "consumer", ":transfer:streaming:streaming-01-http-to-http:streaming-01-runtime"
    ).configurationProvider(fromPropertiesFile(SAMPLE_FOLDER + "/streaming-01-runtime/consumer.properties")));
    private final int httpReceiverPort = Ports.getFreePort();
    private final MockWebServer consumerReceiverServer = new MockWebServer();

    @BeforeEach
    void setUp() throws IOException {
        consumerReceiverServer.start(httpReceiverPort);
    }

    @Test
    void streamData() throws IOException {
        var source = Files.createTempDirectory("source");

        PROVIDER.createAsset(getFileContentFromRelativePath(SAMPLE_FOLDER + "/asset.json")
                .replace("{{sourceFolder}}", source.toString()));
        PROVIDER.createPolicyDefinition(getFileContentFromRelativePath(SAMPLE_FOLDER + "/policy-definition.json"));
        PROVIDER.createContractDefinition(getFileContentFromRelativePath(SAMPLE_FOLDER + "/contract-definition.json"));

        var catalogDatasetId = CONSUMER.fetchDatasetFromCatalog(getFileContentFromRelativePath(SAMPLE_FOLDER + "/get-dataset.json"));
        var negotiateContractBody = getFileContentFromRelativePath(SAMPLE_FOLDER + "/negotiate-contract.json")
                                        .replace("{{offerId}}", catalogDatasetId);
        
        var contractNegotiationId = CONSUMER.negotiateContract(negotiateContractBody);
    
        await().atMost(TIMEOUT).untilAsserted(() -> {
            var contractAgreementId = CONSUMER.getContractAgreementId(contractNegotiationId);
            assertThat(contractAgreementId).isNotNull();
        });
        var contractAgreementId = CONSUMER.getContractAgreementId(contractNegotiationId);
        
        var requestBody = getFileContentFromRelativePath(SAMPLE_FOLDER + "/transfer.json")
                                .replace("{{contract-agreement-id}}", contractAgreementId)
                                .replace("4000", "" + httpReceiverPort);

        var transferProcessId = CONSUMER.startTransfer(requestBody);

        await().atMost(TIMEOUT).untilAsserted(() -> {
            var state = CONSUMER.getTransferProcessState(transferProcessId);
            assertThat(state).isEqualTo(STARTED.name());
        });

        var eventBody = "message that will be sent".getBytes();
        newScheduledThreadPool(1).scheduleAtFixedRate(() -> createMessage(source, eventBody), 0L, 200L, MILLISECONDS);

        await().atMost(TIMEOUT).untilAsserted(() -> {
            var request = consumerReceiverServer.takeRequest();
            assertThat(request).isNotNull();
            assertThat(request.getBody().readByteArray()).isEqualTo(eventBody);
        });
    }

    private static void createMessage(Path source, byte[] content) {
        try {
            Files.write(source.resolve("message-" + UUID.randomUUID()), content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
