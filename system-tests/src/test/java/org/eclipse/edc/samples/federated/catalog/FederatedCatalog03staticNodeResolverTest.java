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

package org.eclipse.edc.samples.federated.catalog;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.CRAWLER_EXECUTION_DELAY_VALUE;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.DATASET_ASSET_ID;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.EMBEDDED_FC_CATALOG_API_ENDPOINT;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.EMPTY_QUERY_FILE_PATH;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.STANDALONE_FC_CATALOG_API_ENDPOINT;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.TIMEOUT;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.createAsset;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.getFcEmbeddedConnector;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.getStandaloneFc;
import static org.eclipse.edc.samples.common.FederatedCatalogCommon.postAndAssertType;
import static org.eclipse.edc.samples.common.FileTransferCommon.getFileContentFromRelativePath;
import static org.eclipse.edc.samples.common.NegotiationCommon.createContractDefinition;
import static org.eclipse.edc.samples.common.NegotiationCommon.createPolicy;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.getProvider;

@EndToEndTest
public class FederatedCatalog03staticNodeResolverTest {

    @RegisterExtension
    static final RuntimeExtension PARTICIPANT_CONNECTOR = getProvider();

    @RegisterExtension
    static final RuntimeExtension STANDALONE_FC_RUNTIME = getStandaloneFc(":federated-catalog:fc-03-static-node-directory:standalone-fc-with-node-resolver");

    @RegisterExtension
    static final RuntimeExtension FC_CONNECTOR = getFcEmbeddedConnector(":federated-catalog:fc-03-static-node-directory:embedded-fc-with-node-resolver");

    @Test
    void shouldStartRuntimes() {
        assertThat(PARTICIPANT_CONNECTOR.getService(Clock.class)).isNotNull();
        assertThat(STANDALONE_FC_RUNTIME.getService(Clock.class)).isNotNull();
        assertThat(FC_CONNECTOR.getService(Clock.class)).isNotNull();
    }

    @Test
    void runSampleSteps() {
        String assetId = createAsset();
        createPolicy();
        createContractDefinition();

        // call catalog API from standalone FC
        await()
                .atMost(Duration.ofSeconds(TIMEOUT))
                .pollDelay(Duration.ofSeconds(CRAWLER_EXECUTION_DELAY_VALUE))
                .ignoreExceptions()
                .until(() -> postAndAssertType(STANDALONE_FC_CATALOG_API_ENDPOINT, getFileContentFromRelativePath(EMPTY_QUERY_FILE_PATH), DATASET_ASSET_ID),
                        id -> id.equals(assetId));

        // call catalog API from embedded FC
        await()
                .atMost(Duration.ofSeconds(TIMEOUT))
                .pollDelay(Duration.ofSeconds(CRAWLER_EXECUTION_DELAY_VALUE))
                .ignoreExceptions()
                .until(() -> postAndAssertType(EMBEDDED_FC_CATALOG_API_ENDPOINT, getFileContentFromRelativePath(EMPTY_QUERY_FILE_PATH), DATASET_ASSET_ID),
                        id -> id.equals(assetId));
    }
}
