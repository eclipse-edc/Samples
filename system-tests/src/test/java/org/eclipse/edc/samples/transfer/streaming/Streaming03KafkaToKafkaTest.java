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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.edc.util.io.Ports;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import static java.time.Duration.ZERO;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcessStates.STARTED;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;
import static org.eclipse.edc.spi.constants.CoreConstants.EDC_NAMESPACE;

@Testcontainers
@EndToEndTest
public class Streaming03KafkaToKafkaTest {

    private static final String TOPIC = "topic-" + UUID.randomUUID();
    private static final String SAMPLE_NAME = "streaming-03-kafka-broker";
    private static final String RUNTIME_NAME = "streaming-03-runtime";
    private static final Path SAMPLE_FOLDER = Path.of("transfer", "streaming", SAMPLE_NAME);
    private static final Path RUNTIME_PATH = SAMPLE_FOLDER.resolve(RUNTIME_NAME);
    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final StreamingParticipant PROVIDER = StreamingParticipant.Builder.newStreamingInstance()
            .name("provider")
            .id("provider")
            .managementEndpoint(new StreamingParticipant.Endpoint(URI.create("http://localhost:18181/management")))
            .protocolEndpoint(new StreamingParticipant.Endpoint(URI.create("http://localhost:18182/protocol")))
            .controlEndpoint(new StreamingParticipant.Endpoint(URI.create("http://localhost:18183/control")))
            .build();
    private static final StreamingParticipant CONSUMER = StreamingParticipant.Builder.newStreamingInstance()
            .name("consumer")
            .id("consumer")
            .managementEndpoint(new StreamingParticipant.Endpoint(URI.create("http://localhost:28181/management")))
            .protocolEndpoint(new StreamingParticipant.Endpoint(URI.create("http://localhost:28182/protocol")))
            .controlEndpoint(new StreamingParticipant.Endpoint(URI.create("http://localhost:28183/control")))
            .build();
    private static final String GROUP_ID = "group_id";

    @Container
    static KafkaContainer kafkaContainer = new KafkaSaslContainer(getFileFromRelativePath(SAMPLE_FOLDER.resolve("kafka.env").toString()))
            .withLogConsumer(frame -> System.out.print(frame.getUtf8String()));

    @RegisterExtension
    static EdcRuntimeExtension providerConnector = new EdcRuntimeExtension(
            ":transfer:streaming:%s:%s".formatted(SAMPLE_NAME, RUNTIME_NAME),
            "provider",
            Map.of(
                    "edc.fs.config",
                    getFileFromRelativePath(RUNTIME_PATH.resolve("provider.properties").toString())
                            .getAbsolutePath()
            )
    );

    @RegisterExtension
    static EdcRuntimeExtension consumerConnector = new EdcRuntimeExtension(
            ":transfer:streaming:%s:%s".formatted(SAMPLE_NAME, RUNTIME_NAME),
            "consumer",
            Map.of(
                    "edc.fs.config",
                    getFileFromRelativePath(RUNTIME_PATH.resolve("consumer.properties").toString())
                            .getAbsolutePath()
            )
    );

    private final int httpReceiverPort = Ports.getFreePort();
    private final MockWebServer edrReceiverServer = new MockWebServer();

    @BeforeEach
    void setUp() throws IOException {
        edrReceiverServer.start(httpReceiverPort);
    }

    @Test
    void streamData() throws InterruptedException, JsonProcessingException {
        createAcls(
                userCanAccess("User:alice", ResourceType.TOPIC, TOPIC),
                userCanAccess("User:alice", ResourceType.GROUP, GROUP_ID)
        );

        PROVIDER.createAsset(getFileContentFromRelativePath(SAMPLE_FOLDER.resolve("1-asset.json").toString())
                .replace("{{bootstrap.servers}}", kafkaContainer.getBootstrapServers())
                .replace("{{topic}}", TOPIC));
        PROVIDER.createPolicyDefinition(getFileContentFromRelativePath(SAMPLE_FOLDER.resolve("2-policy-definition.json").toString()));
        PROVIDER.createContractDefinition(
                getFileContentFromRelativePath(SAMPLE_FOLDER.resolve("3-contract-definition.json").toString()));

        var destination = Json.createObjectBuilder()
                .add("type", "KafkaBroker")
                .build();

        var transferProcessPrivateProperties = Json.createObjectBuilder()
                .add("receiverHttpEndpoint", "http://localhost:" + httpReceiverPort)
                .build();
        var transferProcessId = CONSUMER.requestAssetFrom("kafka-stream-asset", PROVIDER)
                .withPrivateProperties(transferProcessPrivateProperties)
                .withDestination(destination)
                .withTransferType("KafkaBroker-PULL")
                .execute();

        await().atMost(TIMEOUT).untilAsserted(() -> {
            var state = CONSUMER.getTransferProcessState(transferProcessId);
            assertThat(state).isEqualTo(STARTED.name());
        });

        var producer = createKafkaProducer();
        var message = "message";
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> producer
                .send(new ProducerRecord<>(TOPIC, "key", message)), 0L, 100L, MICROSECONDS);

        var endpointDataReference = readEndpointDataReference();

        try (var clientConsumer = createKafkaConsumer(endpointDataReference.getEndpoint(), endpointDataReference.getAuthKey(), endpointDataReference.getAuthCode())) {
            clientConsumer.subscribe(List.of(endpointDataReference.getProperties().get(EDC_NAMESPACE + "topic").toString()));

            await().atMost(TIMEOUT).untilAsserted(() -> {
                var records = clientConsumer.poll(ZERO);
                assertThat(records.isEmpty()).isFalse();
                records.records(TOPIC).forEach(record -> assertThat(record.value()).isEqualTo(message));
            });
        }

        producer.close();
    }

    private EndpointDataReference readEndpointDataReference() throws InterruptedException, JsonProcessingException {
        var request = edrReceiverServer.takeRequest(TIMEOUT.getSeconds(), SECONDS);
        var body = request.getBody().readString(Charset.defaultCharset());
        return new ObjectMapper().readValue(body, EndpointDataReference.class);
    }

    private AclBinding userCanAccess(String principal, ResourceType resourceType, String resourceName) {
        var pattern = new ResourcePattern(resourceType, resourceName, PatternType.LITERAL);
        var entry = new AccessControlEntry(principal, "*", AclOperation.READ, AclPermissionType.ALLOW);
        return new AclBinding(pattern, entry);
    }

    private void createAcls(AclBinding... bindings) {
        var adminProperties = new Properties();
        adminProperties.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        adminProperties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        adminProperties.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin-secret\";");
        adminProperties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        try (var adminClient = AdminClient.create(adminProperties)) {
            adminClient.createAcls(Arrays.stream(bindings).toList()).all().get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Producer<String, String> createKafkaProducer() {
        var props = new Properties();
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin-secret\";");
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

    private static Consumer<String, String> createKafkaConsumer(@NotNull String endpoint, @Nullable String authKey, @Nullable String authCode) {
        var props = new Properties();
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";".formatted(authKey, authCode));
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, endpoint);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        return new KafkaConsumer<>(props);
    }

}
