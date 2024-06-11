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

import dev.failsafe.RetryPolicy;
import org.eclipse.edc.connector.controlplane.transfer.spi.provision.Provisioner;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.DeprovisionedResource;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ProvisionResponse;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ProvisionedResource;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.ResourceDefinition;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.response.StatusResult;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;


public class LocalResourceProvisioner implements Provisioner<LocalResourceDefinition, LocalProvisionedResource> {
    private final Monitor monitor;
    private final RetryPolicy<Object> retryPolicy;
    private final LocalResourceProvisionerConfiguration configuration;

    public LocalResourceProvisioner(Monitor monitor, RetryPolicy<Object> retryPolicy, LocalResourceProvisionerConfiguration configuration) {
        this.monitor = monitor;
        this.configuration = configuration;
        this.retryPolicy = RetryPolicy.builder(retryPolicy.getConfig())
                .withMaxRetries(configuration.maxRetries())
                .build();
    }

    @Override
    public boolean canProvision(ResourceDefinition resourceDefinition) {
        return resourceDefinition instanceof LocalResourceDefinition;
    }

    @Override
    public boolean canDeprovision(ProvisionedResource resourceDefinition) {
        return resourceDefinition instanceof LocalProvisionedResource;
    }

    @Override
    public CompletableFuture<StatusResult<ProvisionResponse>> provision(LocalResourceDefinition resourceDefinition, Policy policy) {
        createDestinationFile(resourceDefinition.getPathName());
        StatusResult<ProvisionResponse> provisionResponseStatusResult = provisionSucceeded(resourceDefinition);
        return completedFuture(provisionResponseStatusResult);
    }

    @Override
    public CompletableFuture<StatusResult<DeprovisionedResource>> deprovision(LocalProvisionedResource provisionedResource, Policy policy) {
        return null;
    }


    private void createDestinationFile(String pathName) {
        var file = new File(pathName.replaceAll("\\.", ".").replaceAll("/", "/"));
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    monitor.debug(String.format("File could not be created at path %s", pathName));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private StatusResult<ProvisionResponse> provisionSucceeded(LocalResourceDefinition resourceDefinition) {
        var resource = LocalProvisionedResource.Builder.newInstance()
                .id(resourceDefinition.getPathName())
                .resourceDefinitionId(resourceDefinition.getId())
                .hasToken(true)
                .pathName(resourceDefinition.getPathName())
                .transferProcessId(resourceDefinition.getTransferProcessId())
                .resourceName(resourceDefinition.getPathName())
                .build();

        monitor.debug("LocalResourceProvisioner: Resource request submitted: " + resourceDefinition.getPathName());

        var response = ProvisionResponse.Builder.newInstance().resource(resource).build();
        return StatusResult.success(response);
    }
}
