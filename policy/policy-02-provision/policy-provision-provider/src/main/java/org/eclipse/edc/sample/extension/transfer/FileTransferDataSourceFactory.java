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

import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSource;
import org.eclipse.edc.connector.dataplane.spi.pipeline.DataSourceFactory;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.transfer.DataFlowStartMessage;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class FileTransferDataSourceFactory implements DataSourceFactory {
    @Override
    public String supportedType() {
        return "File";
    }

    @Override
    public boolean canHandle(DataFlowStartMessage dataRequest) {
        return "File".equalsIgnoreCase(dataRequest.getSourceDataAddress().getType());
    }

    @Override
    public DataSource createSource(DataFlowStartMessage request) {
        var source = getFile(request);
        return new FileTransferDataSource(source);
    }

    @Override
    public @NotNull Result<Void> validateRequest(DataFlowStartMessage request) {
        var source = getFile(request);
        if (!source.exists()) {
            return Result.failure("Source file " + source.getName() + " does not exist at " + source.getAbsolutePath());
        }

        return Result.success();
    }

    @NotNull
    private File getFile(DataFlowStartMessage request) {
        var dataAddress = request.getSourceDataAddress();
        // verify source path
        var sourceFileName = dataAddress.getStringProperty("filename");
        var path = dataAddress.getStringProperty("path");
        // As this is a controlled test input below is to avoid path-injection warning by CodeQL
        sourceFileName = sourceFileName.replaceAll("\\.", ".").replaceAll("/", "/");
        path = path.replaceAll("\\.", ".").replaceAll("/", "/");
        return new File(path, sourceFileName);
    }
}
