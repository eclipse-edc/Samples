package org.eclipse.edc.samples.transfer.streaming;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Extension of the {@link KafkaContainer} that permits to set the SASL_PLAINTEXT security protocol
 */
public class KafkaSaslContainer extends KafkaContainer {

    private static final String KAFKA_IMAGE_NAME = "confluentinc/cp-kafka:7.5.2";
    private final File envFile;

    public KafkaSaslContainer(@NotNull File envFile) {
        super(DockerImageName.parse(KAFKA_IMAGE_NAME));
        this.withKraft();
        this.envFile = envFile;
    }

    @Override
    protected void configureKraft() {
        super.configureKraft();
        try {
            Files.readAllLines(envFile.toPath())
                    .stream().map(it -> it.split("=", 2))
                    .forEach(it -> this.withEnv(it[0], it[1]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
