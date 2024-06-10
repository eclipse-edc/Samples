/*
 *  Copyright (c) 2023 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Sample workflow test
 *       Fraunhofer Institute for Software and Systems Engineering - use current ids instead of placeholder
 *
 */

package org.eclipse.edc.samples.transfer;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.samples.common.NegotiationCommon.createAsset;
import static org.eclipse.edc.samples.common.NegotiationCommon.createContractDefinition;
import static org.eclipse.edc.samples.common.NegotiationCommon.createPolicy;
import static org.eclipse.edc.samples.common.NegotiationCommon.fetchDatasetFromCatalog;
import static org.eclipse.edc.samples.common.NegotiationCommon.getContractAgreementId;
import static org.eclipse.edc.samples.common.NegotiationCommon.negotiateContract;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.getConsumer;
import static org.eclipse.edc.samples.common.PrerequisitesCommon.getProvider;

@EndToEndTest
public class Transfer01negotiationTest {

    @RegisterExtension
    static EdcRuntimeExtension provider = getProvider();

    @RegisterExtension
    static EdcRuntimeExtension consumer = getConsumer();

    private static final String NEGOTIATE_CONTRACT_FILE_PATH = "transfer/transfer-01-negotiation/resources/negotiate-contract.json";
    private static final String FETCH_DATASET_FROM_CATALOG_FILE_PATH = "transfer/transfer-01-negotiation/resources/get-dataset.json";

    @Test
    void runSampleSteps() {
        createAsset();
        createPolicy();
        createContractDefinition();
        var catalogDatasetId = fetchDatasetFromCatalog(FETCH_DATASET_FROM_CATALOG_FILE_PATH);
        var contractNegotiationId = negotiateContract(NEGOTIATE_CONTRACT_FILE_PATH, catalogDatasetId);
        var contractAgreementId = getContractAgreementId(contractNegotiationId);
        assertThat(contractAgreementId).isNotEmpty();
    }
}
