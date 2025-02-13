/*
 *  Copyright (c) 2025 Cofinity-X
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

package org.eclipse.edc.sample.extension.proxy;

import org.eclipse.edc.connector.dataplane.spi.Endpoint;
import org.eclipse.edc.connector.dataplane.spi.iam.DataPlaneAuthorizationService;
import org.eclipse.edc.connector.dataplane.spi.iam.PublicEndpointGeneratorService;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Settings;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.web.spi.WebService;
import org.eclipse.edc.web.spi.configuration.PortMapping;
import org.eclipse.edc.web.spi.configuration.PortMappingRegistry;

import static org.eclipse.edc.web.spi.configuration.ApiContext.PUBLIC;

public class CustomProxyDataPlaneExtension implements ServiceExtension {

    private static final int DEFAULT_PUBLIC_PORT = 8185;
    private static final String DEFAULT_PUBLIC_PATH = "/api/public";

    @Configuration
    private PublicApiConfiguration apiConfiguration;
    @Setting(description = "Base url of the public API endpoint without the trailing slash. This should point to the public endpoint configured.",
            key = "edc.dataplane.proxy.public.endpoint")
    private String proxyPublicEndpoint;

    @Inject
    private PortMappingRegistry portMappingRegistry;
    @Inject
    private PublicEndpointGeneratorService generatorService;
    @Inject
    private WebService webService;
    @Inject
    private DataPlaneAuthorizationService authorizationService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        portMappingRegistry.register(new PortMapping(PUBLIC, apiConfiguration.port(), apiConfiguration.path()));

        generatorService.addGeneratorFunction("HttpData", dataAddress -> Endpoint.url(proxyPublicEndpoint));

        webService.registerResource(PUBLIC, new ProxyController(authorizationService));
    }

    @Settings
    record PublicApiConfiguration(
            @Setting(key = "web.http." + PUBLIC + ".port", description = "Port for " + PUBLIC + " api context", defaultValue = DEFAULT_PUBLIC_PORT + "")
            int port,
            @Setting(key = "web.http." + PUBLIC + ".path", description = "Path for " + PUBLIC + " api context", defaultValue = DEFAULT_PUBLIC_PATH)
            String path
    ) {

    }
}
