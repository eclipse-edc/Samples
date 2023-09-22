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
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.query.Criterion;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.types.domain.asset.Asset;

import java.util.List;

import static org.eclipse.edc.policy.engine.spi.PolicyEngine.ALL_SCOPES;

@Extension(value = PolicyFunctionsExtension.NAME)
public class PolicyFunctionsExtension implements ServiceExtension {
    private static final String FILE_PATH = "edc.samples.policy-02.constraint.desired.file.path";
    private static final String KEY = "POLICY_REGULATE_FILE_PATH";
    public static final String NAME = "Policy Functions Extension";
    public static final String POLICY_TYPE = "USE";
    public static final String RIGHT_OPERAND = "test-document";
    public static final String DEFAULT_FILE_PATH = "/tmp/desired/path/transfer.txt";

    @Inject
    private RuleBindingRegistry ruleBindingRegistry;
    @Inject
    private PolicyDefinitionStore policyStore;
    @Inject
    private ContractDefinitionStore contractDefinitionStore;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
        ruleBindingRegistry.bind(POLICY_TYPE, ALL_SCOPES);

        registerContractDefinition(context);
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
        var desiredFilePath = context.getSetting(FILE_PATH, DEFAULT_FILE_PATH);
        var regulateFilePathConstraint = AtomicConstraint.Builder.newInstance()
                .leftExpression(new LiteralExpression(KEY))
                .operator(Operator.EQ)
                .rightExpression(new LiteralExpression(desiredFilePath))
                .build();


        var permission = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type(POLICY_TYPE).build())
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
                                .operator("=") // TODO changed to EQ?
                                .operandRight(RIGHT_OPERAND)
                                .build()))
                .build();
        contractDefinitionStore.save(contractDefinition);
    }

}
