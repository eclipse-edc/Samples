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

import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSourceFactory;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

/**
 * Handles `HttpStreaming` sources, validating the address and creating the specific {@link HttpStreamingDataSource}
 * instance on every {@link TransferProcess} started.
 */
public class HttpStreamingDataSourceFactory implements DataSourceFactory {

    @Override
    public boolean canHandle(DataFlowStartMessage dataFlowStartMessage) {
        return dataFlowStartMessage.getSourceDataAddress().getType().equals("HttpStreaming");
    }

    @Override
    public DataSource createSource(DataFlowStartMessage dataFlowStartMessage) {
        return new HttpStreamingDataSource(sourceFolder(dataFlowStartMessage).get());
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowStartMessage dataFlowStartMessage) {
        return sourceFolder(dataFlowStartMessage)
                .map(it -> Result.success())
                .orElseGet(() -> Result.failure("sourceFolder is not found or it does not exist"));
    }

    private Optional<File> sourceFolder(DataFlowStartMessage request) {
        return Optional.of(request)
                .map(DataFlowStartMessage::getSourceDataAddress)
                .map(it -> it.getStringProperty("sourceFolder"))
                .map(File::new)
                .filter(File::exists);
    }
}
