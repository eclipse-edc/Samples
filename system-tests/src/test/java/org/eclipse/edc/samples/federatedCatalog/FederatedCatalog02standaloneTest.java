/*
 *  Copyright (c) 2024 Fraunhofer-Gesellschaft
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer-Gesellschaft - initial API and implementation
 *
 */

package org.eclipse.edc.samples.federatedCatalog;

import org.assertj.core.api.Assertions;
import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Clock;
import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.*;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.NegotiationCommon.createContractDefinition;
import static org.eclipse.edc.samples.common.NegotiationCommon.createPolicy;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.*;

@EndToEndTest
public class FederatedCatalog02standaloneTest {
    @RegisterExtension
    static RuntimeExtension participantConnector = getProvider();

    @RegisterExtension
    static RuntimeExtension standaloneFcRuntime = getStandaloneFc();

    @Test
    void shouldStartRuntimes() {
        Assertions.assertThat(participantConnector.getService(Clock.class)).isNotNull();
        Assertions.assertThat(standaloneFcRuntime.getService(Clock.class)).isNotNull();
    }

    @Test
    void runSampleSteps() {
        String assetId = createAsset();
        createPolicy();
        createContractDefinition();

        await()
                .atMost(Duration.ofSeconds(TIMEOUT))
                .pollDelay(Duration.ofSeconds(CRAWLER_EXECUTION_DELAY_VALUE))
                .pollInterval(Duration.ofSeconds(CRAWLER_EXECUTION_PERIOD_VALUE))
                .ignoreExceptions()
                .until(() -> postAndAssertType(STANDALONE_FC_CATALOG_API_ENDPOINT, getFileContentFromRelativePath(EMPTY_QUERY_FILE_PATH), DATASET_ASSET_ID),
                        id -> id.equals(assetId));
    }

}
