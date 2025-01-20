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
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.edc.connector.controlplane.test.system.utils.LazySupplier;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.eclipse.edc.util.io.Ports;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates.STARTED;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.util.ConfigPropertiesLoader.fromPropertiesFile;

@Testcontainers
@EndToEndTest
public class Streaming02KafkaToHttpTest {

    private static final String KAFKA_IMAGE_NAME = "confluentinc/cp-kafka:7.4.0";
    private static final String TOPIC = "kafka-stream-topic";
    private static final String MAX_DURATION = "PT30S";
    private static final String SAMPLE_FOLDER = "transfer/streaming/streaming-02-kafka-to-http";
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

    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse(KAFKA_IMAGE_NAME))
            .withKraft()
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_CREATE_TOPICS", TOPIC.concat(":1:1"));

    @RegisterExtension
    static RuntimeExtension providerConnector = new RuntimePerClassExtension(new EmbeddedRuntime(
            "provider",
            ":transfer:streaming:streaming-02-kafka-to-http:streaming-02-runtime"
    ).configurationProvider(fromPropertiesFile(SAMPLE_FOLDER + "/streaming-02-runtime/provider.properties")));

    @RegisterExtension
    static RuntimeExtension consumerConnector = new RuntimePerClassExtension(new EmbeddedRuntime(
            "consumer",
            ":transfer:streaming:streaming-02-kafka-to-http:streaming-02-runtime"
    ).configurationProvider(fromPropertiesFile(SAMPLE_FOLDER + "/streaming-02-runtime/consumer.properties")));

    private final int httpReceiverPort = Ports.getFreePort();
    private final MockWebServer consumerReceiverServer = new MockWebServer();

    @BeforeEach
    void setUp() throws IOException {
        consumerReceiverServer.start(httpReceiverPort);
    }

    @Test
    void streamData() {
        PROVIDER.createAsset(getFileContentFromRelativePath(SAMPLE_FOLDER + "/1-asset.json")
                .replace("{{bootstrap.servers}}", kafkaContainer.getBootstrapServers())
                .replace("{{max.duration}}", MAX_DURATION)
                .replace("{{topic}}", TOPIC));
        PROVIDER.createPolicyDefinition(getFileContentFromRelativePath(SAMPLE_FOLDER + "/2-policy-definition.json"));
        PROVIDER.createContractDefinition(
                getFileContentFromRelativePath(SAMPLE_FOLDER + "/3-contract-definition.json"));

        var destination = Json.createObjectBuilder()
                .add("type", "HttpData")
                .add("baseUrl", "http://localhost:" + httpReceiverPort)
                .build();

        var transferProcessId = CONSUMER.requestAssetFrom("kafka-stream-asset", PROVIDER)
                .withDestination(destination)
                .withTransferType("HttpData-PUSH")
                .execute();

        await().atMost(TIMEOUT).untilAsserted(() -> {
            var state = CONSUMER.getTransferProcessState(transferProcessId);
            assertThat(state).isEqualTo(STARTED.name());
        });

        var producer = createKafkaProducer();
        var message = "message";
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> producer
                .send(new ProducerRecord<>(TOPIC, "key", message)), 0L, 100L, MICROSECONDS);

        await().atMost(TIMEOUT).untilAsserted(() -> {
            var request = consumerReceiverServer.takeRequest();
            assertThat(request).isNotNull();
            assertThat(request.getBody().readByteArray()).isEqualTo(message.getBytes());
        });

        producer.close();
    }

    private Producer<String, String> createKafkaProducer() {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

}
