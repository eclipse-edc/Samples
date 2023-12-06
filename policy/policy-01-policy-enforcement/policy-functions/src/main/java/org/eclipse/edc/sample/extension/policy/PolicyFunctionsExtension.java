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
import org.eclipse.edc.policy.engine.spi.PolicyContext;
import org.eclipse.edc.policy.engine.spi.PolicyContextImpl;
import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.*;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.asset.Asset;

import java.nio.file.Path;

import static org.eclipse.edc.policy.engine.spi.PolicyEngine.ALL_SCOPES;

public class PolicyFunctionsExtension implements ServiceExtension {
    private final String policyTimeKey = "POLICY_EVALUATION_TIME";
    private final String policyStartDateSetting = "edc.samples.policy-01.constraint.date.start";
    private final String policyEndDateSetting = "edc.samples.policy-01.constraint.date.end";
    private static final String EDC_ASSET_PATH = "edc.samples.policy-01.asset.path";
    private static final String LOCATION_POLICY_ID = "eu-policy";
    private static final String LOCATION_CONSTRAINT_KEY = "locationConstraints";

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
        return "Policy - contract-negotiation policies";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();
        PolicyContext policyContext = new PolicyContextImpl();

        ruleBindingRegistry.bind("USE", ALL_SCOPES);
        ruleBindingRegistry.bind("region", ALL_SCOPES);
        policyEngine.registerFunction(ALL_SCOPES, Permission.class, "region", new LocationConstraintFunction(monitor, policyContext));

        registerDataEntries(context);
        registerContractDefinition(context);
        registerPolicy();
        registerAsset();

        context.getMonitor().info("Policy Extension for Policy Sample (contract-negotiation) initialized!");
    }

    private PolicyDefinition createAccessPolicy() {

        var usePermission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type("USE").build())
                .build();

        return PolicyDefinition.Builder.newInstance()
                .id("use")
                .policy(Policy.Builder.newInstance()
                        .permission(usePermission)
                        .build())
                .build();
    }

    private PolicyDefinition createContractPolicy(ServiceExtensionContext context) {
        var regionSetting = context.getSetting("edc.samples.policy-01.constraint.location.region", "eu");
        var locationConstraint = AtomicConstraint.Builder.newInstance()
                .leftExpression(new LiteralExpression("region"))
                .operator(Operator.EQ)
                .rightExpression(new LiteralExpression(regionSetting))
                .build();

        var permission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type("USE").build())
                .constraint(locationConstraint)
                .build();

        return PolicyDefinition.Builder.newInstance()
                .id("use-region-restricted")
                .policy(Policy.Builder.newInstance()
                        .permission(permission)
                        .build())
                .build();
    }


    private void registerDataEntries(ServiceExtensionContext context) {
        var assetPathSetting = context.getSetting(EDC_ASSET_PATH, "/tmp/provider/test-document.txt");
        var assetPath = Path.of(assetPathSetting);

        var dataAddress = DataAddress.Builder.newInstance()
                .property("type", "File")
                .property("path", assetPath.getParent().toString())
                .property("filename", assetPath.getFileName().toString())
                .build();

        var assetId = "test-document";
        var asset = Asset.Builder.newInstance()
                .id(assetId)
                .dataAddress(dataAddress)
                .build();


        assetIndex.create(asset);

    }


    private void registerContractDefinition(ServiceExtensionContext context) {
        var accessPolicy = createAccessPolicy();
        policyStore.create(accessPolicy);

        var contractPolicy = createContractPolicy(context);
        policyStore.create(contractPolicy);

        var contractDefinition = ContractDefinition.Builder.newInstance()
                .id("1")
                .accessPolicyId(accessPolicy.getUid())
                .contractPolicyId(contractPolicy.getUid())
                .assetsSelectorCriterion(Criterion.criterion(Asset.PROPERTY_ID, "=", "test-document"))
                .build();
        contractDefinitionStore.save(contractDefinition);
    }

    private void registerPolicy() {
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

    private void registerAsset() {
        var dataAddress = DataAddress.Builder.newInstance()
                .property("type", "test")
                .build();
        var assetId = "test-document";
        var asset = Asset.Builder.newInstance()
                .id(assetId)
                .dataAddress(dataAddress)
                .build();

        assetIndex.create(asset);
    }
}
