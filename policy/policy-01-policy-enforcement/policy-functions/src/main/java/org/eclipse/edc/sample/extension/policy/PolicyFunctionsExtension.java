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
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.asset.AssetIndex;
import org.eclipse.edc.spi.asset.AssetSelectorExpression;
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

        ruleBindingRegistry.bind("USE", ALL_SCOPES);
        ruleBindingRegistry.bind(policyTimeKey, ALL_SCOPES);
        policyEngine.registerFunction(ALL_SCOPES, Permission.class, policyTimeKey, new TimeIntervalFunction(monitor));

        registerDataEntries(context);
        registerContractDefinition(context);

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
        var startDate = context.getSetting(policyStartDateSetting, "2023-01-01T00:00:00.000+02:00");
        var notBeforeConstraint = AtomicConstraint.Builder.newInstance()
                .leftExpression(new LiteralExpression(policyTimeKey))
                .operator(Operator.GT)
                .rightExpression(new LiteralExpression(startDate))
                .build();

        var endDate = context.getSetting(policyEndDateSetting, "2023-12-31T00:00:00.000+02:00");
        var notAfterConstraint = AtomicConstraint.Builder.newInstance()
                .leftExpression(new LiteralExpression(policyTimeKey))
                .operator(Operator.LT)
                .rightExpression(new LiteralExpression(endDate))
                .build();


        var permission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type("USE").build())
                .constraint(notBeforeConstraint)
                .constraint(notAfterConstraint)
                .build();


        return PolicyDefinition.Builder.newInstance()
                .id("use-time-restricted")
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
        var asset = Asset.Builder.newInstance().id(assetId).build();

        assetIndex.accept(asset, dataAddress);
    }

    private void registerContractDefinition(ServiceExtensionContext context) {
        var accessPolicy = createAccessPolicy();
        policyStore.save(accessPolicy);

        var contractPolicy = createContractPolicy(context);
        policyStore.save(contractPolicy);

        var contractDefinition = ContractDefinition.Builder.newInstance()
                .id("1")
                .accessPolicyId(accessPolicy.getUid())
                .contractPolicyId(contractPolicy.getUid())
                .selectorExpression(AssetSelectorExpression.Builder.newInstance()
                        .whenEquals(Asset.PROPERTY_ID, "test-document")
                        .build())
                .validity(31536000)
                .build();
        contractDefinitionStore.save(contractDefinition);
    }

}
