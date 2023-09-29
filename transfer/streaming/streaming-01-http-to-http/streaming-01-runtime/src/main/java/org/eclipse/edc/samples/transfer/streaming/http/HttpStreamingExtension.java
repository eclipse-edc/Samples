/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial test implementation for sample
 *
 */

package org.eclipse.edc.samples.transfer.streaming.http;

import org.eclipse.edc.connector.dataplane.spi.pipeline.PipelineService;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

/**
 * Register the {@link HttpStreamingDataSourceFactory} to the data plane pipeline service
 */
public class HttpStreamingExtension implements ServiceExtension {

    @Override
    public String name() {
        return "Http Streaming";
    }

    @Inject
    private PipelineService pipelineService;

    @Override
    public void initialize(ServiceExtensionContext context) {
        pipelineService.registerFactory(new HttpStreamingDataSourceFactory());
    }
}
