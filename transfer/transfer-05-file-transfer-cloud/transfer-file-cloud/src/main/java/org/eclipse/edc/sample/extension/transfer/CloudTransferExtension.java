/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - Initial implementation
 *
 */

package org.eclipse.edc.sample.extension.transfer;

import org.eclipse.edc.connector.controlplane.asset.spi.domain.Asset;
import org.eclipse.edc.connector.controlplane.asset.spi.index.AssetIndex;
import org.eclipse.edc.connector.controlplane.contract.spi.offer.store.ContractDefinitionStore;
import org.eclipse.edc.connector.controlplane.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.controlplane.policy.spi.PolicyDefinition;
import org.eclipse.edc.connector.controlplane.policy.spi.store.PolicyDefinitionStore;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.domain.DataAddress;

import static org.eclipse.edc.spi.query.Criterion.criterion;

public class CloudTransferExtension implements ServiceExtension {
    @Inject
    private AssetIndex assetIndex;
    @Inject
    private PolicyDefinitionStore policyDefinitionStore;
    @Inject
    private ContractDefinitionStore contractDefinitionStore;

    @Override
    public String name() {
        return "Cloud-Based Transfer";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var policy = createPolicy();
        policyDefinitionStore.create(policy);

        registerDataEntries();
        registerContractDefinition(policy.getId());
    }

    public void registerDataEntries() {
        var dataAddress = DataAddress.Builder.newInstance()
                .type("AzureStorage")
                .property("@type", "DataAddress")
                .property("account", "provider")
                .property("container", "src-container")
                .property("blobName", "test-document.txt")
                .keyName("provider-key")
                .build();
        var asset = Asset.Builder.newInstance().id("1").dataAddress(dataAddress).build();
        assetIndex.create(asset);

        var dataAddress2 = DataAddress.Builder.newInstance()
                .type("AzureStorage")
                .property("@type", "DataAddress")
                .property("account", "provider")
                .property("container", "src-container")
                .property("blobName", "test-document.txt")
                .keyName("provider-key")
                .build();
        var asset2 = Asset.Builder.newInstance().id("2").dataAddress(dataAddress2).build();
        assetIndex.create(asset2);
    }

    public void registerContractDefinition(String policyId) {
        var contractDefinition1 = ContractDefinition.Builder.newInstance()
                .id("1")
                .accessPolicyId(policyId)
                .contractPolicyId(policyId)
                .assetsSelectorCriterion(criterion(Asset.PROPERTY_ID, "=", "1"))
                .build();

        var contractDefinition2 = ContractDefinition.Builder.newInstance()
                .id("2")
                .accessPolicyId(policyId)
                .contractPolicyId(policyId)
                .assetsSelectorCriterion(criterion(Asset.PROPERTY_ID, "=", "2"))
                .build();

        contractDefinitionStore.save(contractDefinition1);
        contractDefinitionStore.save(contractDefinition2);
    }

    private PolicyDefinition createPolicy() {
        var usePermission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type("USE").build())
                .build();

        return PolicyDefinition.Builder.newInstance()
                .policy(Policy.Builder.newInstance()
                        .permission(usePermission)
                        .build())
                .build();
    }
}
