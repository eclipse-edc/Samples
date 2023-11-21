/*
 *  Copyright (c) 2023 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

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
