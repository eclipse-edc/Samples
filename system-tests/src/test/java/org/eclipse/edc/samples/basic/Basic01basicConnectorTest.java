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
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.samples.basic;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

@EndToEndTest
class Basic01basicConnectorTest {

    @RegisterExtension
    static RuntimeExtension connector = new RuntimePerClassExtension(new EmbeddedRuntime(
            "connector",
            ":basic:basic-01-basic-connector"
    ));

    @Test
    void shouldStartConnector() {
        assertThat(connector.getService(Clock.class)).isNotNull();
    }
}
