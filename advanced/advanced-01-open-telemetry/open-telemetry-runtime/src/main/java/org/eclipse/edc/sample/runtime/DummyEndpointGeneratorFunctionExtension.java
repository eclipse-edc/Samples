/*
 *  Copyright (c) 2026 Cofinity-X
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Cofinity-X - initial API and implementation
 *
 */

package org.eclipse.edc.sample.runtime;

import org.eclipse.edc.connector.dataplane.spi.Endpoint;
import org.eclipse.edc.connector.dataplane.spi.iam.PublicEndpointGeneratorService;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class DummyEndpointGeneratorFunctionExtension implements ServiceExtension {

    @Inject
    private PublicEndpointGeneratorService publicEndpointGeneratorService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        publicEndpointGeneratorService.addGeneratorFunction("HttpData", e -> Endpoint.url("http://localhost/dummy"));
    }
}
