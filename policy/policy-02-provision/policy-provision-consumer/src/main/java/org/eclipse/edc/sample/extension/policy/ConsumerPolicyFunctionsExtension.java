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

import org.eclipse.edc.policy.engine.spi.PolicyEngine;
import org.eclipse.edc.policy.engine.spi.RuleBindingRegistry;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import static org.eclipse.edc.connector.transfer.spi.provision.ResourceManifestGenerator.MANIFEST_VERIFICATION_SCOPE;
import static org.eclipse.edc.policy.engine.spi.PolicyEngine.ALL_SCOPES;

public class ConsumerPolicyFunctionsExtension implements ServiceExtension {
    private final String policyRegulateFilePath = "POLICY_REGULATE_FILE_PATH";
    @Inject
    private RuleBindingRegistry ruleBindingRegistry;
    @Inject
    private PolicyEngine policyEngine;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var monitor = context.getMonitor();

        ruleBindingRegistry.bind("USE", ALL_SCOPES);

        ruleBindingRegistry.bind(policyRegulateFilePath, MANIFEST_VERIFICATION_SCOPE);
        policyEngine.registerFunction(MANIFEST_VERIFICATION_SCOPE, Permission.class, policyRegulateFilePath, new RegulateFilePathFunction(monitor));

        context.getMonitor().info("File Transfer Extension for Consumer Policy Sample initialized!");
    }

    @Override
    public String name() {
        return "Provision Policy Samples Consumer Policies";
    }

}
