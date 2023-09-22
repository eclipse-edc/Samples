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
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.domain.asset.Asset;

import java.util.List;

import static org.eclipse.edc.policy.engine.spi.PolicyEngine.ALL_SCOPES;

public class PolicyFunctionsExtension implements ServiceExtension {
    private final String policyRegulatedFilePathSetting = "edc.samples.policy-02.constraint.desired.file.path";
    private final String policyRegulateFilePath = "POLICY_REGULATE_FILE_PATH";

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;

    @Inject
    private PolicyDefinitionStore policyStore;

    @Inject
    private ContractDefinitionStore contractDefinitionStore;

    @Override
    public String name() {
        return "Policy - provision policies";
    }

    @Override
    public void initialize(ServiceExtensionContext context) {

        ruleBindingRegistry.bind("USE", ALL_SCOPES);

        registerContractDefinition(context);

        context.getMonitor().info("Policy Extension for Policy Sample (provision) initialized!");
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
        var desiredFilePath = context.getSetting(policyRegulatedFilePathSetting, "/tmp/desired/path/transfer.txt");
        var regulateFilePathConstraint = AtomicConstraint.Builder.newInstance()
                .leftExpression(new LiteralExpression(policyRegulateFilePath))
                .operator(Operator.EQ)
                .rightExpression(new LiteralExpression(desiredFilePath))
                .build();


        var permission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type("USE").build())
                .constraint(regulateFilePathConstraint)
                .build();


        return PolicyDefinition.Builder.newInstance()
                .id("use-regulated-path")
                .policy(Policy.Builder.newInstance()
                        .permission(permission)
                        .build())
                .build();
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
                .assetsSelector(List.of(Criterion.Builder.newInstance()
                                .operandLeft(Asset.PROPERTY_ID)
                                .operator("=")
                                .operandRight("test-document")
                                .build()))
                .build();
        contractDefinitionStore.save(contractDefinition);
    }

}
