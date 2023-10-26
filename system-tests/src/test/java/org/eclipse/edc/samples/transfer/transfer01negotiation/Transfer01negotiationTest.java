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
 *
 */

package org.eclipse.edc.samples.transfer.transfer01negotiation;

import org.eclipse.edc.junit.annotations.EndToEndTest;
import org.eclipse.edc.junit.extensions.EdcRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.createAsset;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.createContractDefinition;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.createPolicy;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.fetchCatalog;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.getContractAgreementId;
import static org.eclipse.edc.samples.transfer.transfer01negotiation.NegotiationCommon.negotiateContract;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.getConsumer;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.getProvider;
import static org.eclipse.edc.samples.transfer.transfer00prerequisites.PrerequisitesCommon.runPrerequisites;

@EndToEndTest
public class Transfer01negotiationTest {

    @RegisterExtension
    static EdcRuntimeExtension provider = getProvider();

    @RegisterExtension
    static EdcRuntimeExtension consumer = getConsumer();

    @Test
    void runSampleSteps() {
        runPrerequisites();
        createAsset();
        createPolicy();
        createContractDefinition();
        fetchCatalog();
        var contractNegotiationId = negotiateContract();
        var contractAgreementId = getContractAgreementId(contractNegotiationId);
        assertThat(contractAgreementId).isNotEmpty();
    }
}
