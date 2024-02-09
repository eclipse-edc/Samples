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

package org.eclipse.edc.sample.extension.policy;

import org.eclipse.edc.connector.contract.spi.offer.store.ContractDefinitionStore;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractDefinition;
import org.eclipse.edc.connector.policy.spi.PolicyDefinition;
import org.eclipse.edc.connector.policy.spi.store.PolicyDefinitionStore;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.asset.Asset;

import static org.eclipse.edc.policy.engine.spi.PolicyEngine.ALL_SCOPES;

public class PolicyFunctionsExtension implements ServiceExtension {
    private static final String ASSET_ID = "test-document";
    private static final String LOCATION_POLICY_ID = "eu-policy";
    private static final String LOCATION_CONSTRAINT_KEY = "location";
    
    @Inject
    private RuleBindingRegistry ruleBindingRegistry;
    @Inject
    private PolicyEngine policyEngine;
    @Inject
    private PolicyDefinitionStore policyStore;
    @Inject
    private ContractDefinitionStore contractDefinitionStore;
    @Inject
    private AssetIndex assetIndex;
    
    @Override
    public String name() {
        return "Sample policy functions";
    }
    
    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        
        ruleBindingRegistry.bind("USE", ALL_SCOPES);
        ruleBindingRegistry.bind(LOCATION_CONSTRAINT_KEY, ALL_SCOPES);
        policyEngine.registerFunction(ALL_SCOPES, Permission.class, LOCATION_CONSTRAINT_KEY, new LocationConstraintFunction(monitor));
        
        registerAsset();
        registerRestrictedPolicy();
        registerContractDefinition();
    }
    
    private void registerAsset() {
        var dataAddress = DataAddress.Builder.newInstance()
                .property("type", "test")
                .build();
        var asset = Asset.Builder.newInstance()
                .id(ASSET_ID)
                .dataAddress(dataAddress)
                .build();
        
        assetIndex.create(asset);
    }
    
    private void registerRestrictedPolicy() {
        var locationConstraint = AtomicConstraint.Builder.newInstance()
                .leftExpression(new LiteralExpression(LOCATION_CONSTRAINT_KEY))
                .operator(Operator.EQ)
                .rightExpression(new LiteralExpression("eu"))
                .build();
        var permission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type("USE").build())
                .constraint(locationConstraint)
                .build();
        var policyDefinition = PolicyDefinition.Builder.newInstance()
                .id(LOCATION_POLICY_ID)
                .policy(Policy.Builder.newInstance()
                        .type(PolicyType.SET)
                        .permission(permission)
                        .build())
                .build();
        
        policyStore.create(policyDefinition);
    }
    
    private void registerContractDefinition() {
        var contractDefinition = ContractDefinition.Builder.newInstance()
                .id("1")
                .accessPolicyId(LOCATION_POLICY_ID)
                .contractPolicyId(LOCATION_POLICY_ID)
                .assetsSelectorCriterion(Criterion.criterion(Asset.PROPERTY_ID, "=", ASSET_ID))
                .build();
        
        contractDefinitionStore.save(contractDefinition);
    }
}
