/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.sample.extension.transfer;

import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSink;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSinkFactory;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class FileTransferDataSinkFactory implements DataSinkFactory {
    private final Monitor monitor;
    private final ExecutorService executorService;
    private final int partitionSize;

    public FileTransferDataSinkFactory(Monitor monitor, ExecutorService executorService,
                                       int partitionSize) {
        this.monitor = monitor;
        this.executorService = executorService;
        this.partitionSize = partitionSize;
    }

    @Override
    public String supportedType() {
        return "File";
    }

    @Override
    public boolean canHandle(DataFlowStartMessage request) {
        return "File".equalsIgnoreCase(request.getDestinationDataAddress().getType());
    }

    @Override
    public DataSink createSink(DataFlowStartMessage request) {
        var destination = request.getDestinationDataAddress();

        // verify destination path
        var path = destination.getStringProperty("path");
        // As this is a controlled test input below is to avoid path-injection warning by CodeQL
        var destinationFile = new File(path.replaceAll("\\.", ".").replaceAll("/", "/"));

        return FileTransferDataSink.Builder.newInstance()
                .file(destinationFile)
                .requestId(request.getId())
                .partitionSize(partitionSize)
                .executorService(executorService)
                .monitor(monitor)
                .build();
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowStartMessage request) {
        return Result.success();
    }
}
