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
 *       Microsoft Corporation - initial test implementation for sample
 *
 */

package org.eclipse.edc.samples.transfer.transfer00prerequisites;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.*;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.runNegotiation;
import static org.eclipse.edc.samples.transfer.transfer02consumerpull.ConsumerPullCommon.startConsumerPullTransfer;

@EndToEndTest
public class Transfer00prerequisitesTest {

    @RegisterExtension
    static EdcRuntimeExtension provider = getProvider();

    @RegisterExtension
    static EdcRuntimeExtension consumer = getConsumer();

    @Test
    void runSampleSteps() {
        registerDataPlaneProvider();
        registerDataPlaneConsumer();
    }
}
