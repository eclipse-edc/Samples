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

package org.eclipse.edc.sample.extension.provision;

import org.eclipse.edc.connector.controlplane.transfer.spi.provision.ConsumerResourceDefinitionGenerator;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ResourceDefinition;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcess;
import org.eclipse.edc.policy.model.Policy;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static java.util.UUID.randomUUID;

public class LocalConsumerResourceDefinitionGenerator implements ConsumerResourceDefinitionGenerator {

    private static final String TYPE = "File";
    /**
     * This will get modified during the policy evaluation to notice the change, keep the path different from the path used in policy
     */
    private static final String DESTINATION = "any path";

    @Override
    public @Nullable ResourceDefinition generate(TransferProcess transferProcess, Policy policy) {
        Objects.requireNonNull(transferProcess, "transferProcess must always be provided");
        Objects.requireNonNull(policy, "policy must always be provided");

        return LocalResourceDefinition.Builder.newInstance()
                .id(randomUUID().toString())
                .pathName(DESTINATION)
                .build();
    }

    @Override
    public boolean canGenerate(TransferProcess transferProcess, Policy policy) {
        Objects.requireNonNull(transferProcess, "dataRequest must always be provided");
        Objects.requireNonNull(policy, "policy must always be provided");

        return TYPE.equals(transferProcess.getDestinationType());
    }

}
