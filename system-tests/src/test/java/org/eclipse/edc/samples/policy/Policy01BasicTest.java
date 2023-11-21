package org.eclipse.edc.samples.policy;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Clock;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@EndToEndTest
class Policy01BasicTest {
    @RegisterExtension
    static EdcRuntimeExtension consumer = new EdcRuntimeExtension(
            ":policy:policy-01-policy-enforcement:policy-enforcement-consumer",
            "consumer",
            emptyMap()
    );
    @Test
    void shouldStartConsumer() {
        assertThat(consumer.getContext().getService(Clock.class)).isNotNull();
    }
}
