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

import jakarta.json.Json;
import okhttp3.mockwebserver.MockWebServer;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.eclipse.edc.junit.testfixtures.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates.STARTED;
import static org.eclipse.edc.samples.transfer.FileTransferSampleTestCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.transfer.FileTransferSampleTestCommon.getFileFromRelativePath;

@EndToEndTest
public class Streaming01httpToHttpTest {

    private static final String SAMPLE_FOLDER = "transfer/streaming/streaming-01-http-to-http";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private static final Participant PROVIDER = Participant.Builder.newInstance()
            .name("provider")
            .id("provider")
            .managementEndpoint(new Participant.Endpoint(URI.create("http://localhost:18181/management")))
            .protocolEndpoint(new Participant.Endpoint(URI.create("http://localhost:18182/protocol")))
            .controlEndpoint(new Participant.Endpoint(URI.create("http://localhost:18183/control")))
            .build();

    private static final Participant CONSUMER = Participant.Builder.newInstance()
            .name("consumer")
            .id("consumer")
            .managementEndpoint(new Participant.Endpoint(URI.create("http://localhost:28181/management")))
            .protocolEndpoint(new Participant.Endpoint(URI.create("http://localhost:28182/protocol")))
            .controlEndpoint(new Participant.Endpoint(URI.create("http://localhost:28183/control")))
            .build();

    @RegisterExtension
    static EdcRuntimeExtension providerConnector = new EdcRuntimeExtension(
            ":transfer:streaming:streaming-01-http-to-http:streaming-01-runtime",
            "provider",
            Map.of(
                    "edc.fs.config", getFileFromRelativePath(SAMPLE_FOLDER + "/streaming-01-runtime/provider.properties").getAbsolutePath()
            )
    );

    @RegisterExtension
    static EdcRuntimeExtension consumerConnector = new EdcRuntimeExtension(
            ":transfer:streaming:streaming-01-http-to-http:streaming-01-runtime",
            "provider",
            Map.of(
                    "edc.fs.config", getFileFromRelativePath(SAMPLE_FOLDER + "/streaming-01-runtime/consumer.properties").getAbsolutePath()
            )
    );
    private final int httpReceiverPort = TestUtils.getFreePort();
    private final MockWebServer consumerReceiverServer = new MockWebServer();

    @BeforeEach
    void setUp() throws IOException {
        consumerReceiverServer.start(httpReceiverPort);
    }

    @Test
    void streamData() throws IOException {
        var source = Files.createTempDirectory("source");
        PROVIDER.registerDataPlane(List.of("HttpStreaming"), List.of("HttpData"));

        PROVIDER.createAsset(getFileContentFromRelativePath(SAMPLE_FOLDER + "/asset.json")
                .replace("{{sourceFolder}}", source.toString()));
        PROVIDER.createPolicyDefinition(getFileContentFromRelativePath(SAMPLE_FOLDER + "/policy-definition.json"));
        PROVIDER.createContractDefinition(getFileContentFromRelativePath(SAMPLE_FOLDER + "/contract-definition.json"));

        var destination = Json.createObjectBuilder()
                .add("type", "HttpData")
                .add("baseUrl", "http://localhost:" + httpReceiverPort)
                .build();
        var transferProcessId = CONSUMER.requestAsset(PROVIDER, "stream-asset", Json.createObjectBuilder().build(), destination);

        await().atMost(TIMEOUT).untilAsserted(() -> {
            String state = CONSUMER.getTransferProcessState(transferProcessId);
            assertThat(state).isEqualTo(STARTED.name());
        });

        var eventBody = "message that will be sent".getBytes();
        Files.write(source.resolve("message-" + UUID.randomUUID()), eventBody);

        await().atMost(TIMEOUT).untilAsserted(() -> {
            var request = consumerReceiverServer.takeRequest();
            assertThat(request).isNotNull();
            assertThat(request.getBody().readByteArray()).isEqualTo(eventBody);
        });
    }
}
