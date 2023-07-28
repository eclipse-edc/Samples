/*
 *  Copyright (c) 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - Initial implementation
 *
 */

package org.eclipse.edc.sample.extension.listener;

import org.eclipse.edc.connector.transfer.spi.observe.TransferProcessObservable;
import org.eclipse.edc.connector.transfer.spi.status.StatusCheckerRegistry;
import org.eclipse.edc.connector.transfer.spi.types.ProvisionedResource;
import org.eclipse.edc.connector.transfer.spi.types.StatusChecker;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.io.File;
import java.util.List;

public class TransferListenerExtension implements ServiceExtension {

    @Inject
    private StatusCheckerRegistry statusCheckerRegistry;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var transferProcessObservable = context.getService(TransferProcessObservable.class);
        var monitor = context.getMonitor();

        transferProcessObservable.registerListener(new MarkerFileCreator(monitor));
        statusCheckerRegistry.register("File", new FileStatusChecker());
    }

    private static class FileStatusChecker implements StatusChecker {
        @Override
        public boolean isComplete(TransferProcess transferProcess, List<ProvisionedResource> resources) {
            var path = transferProcess.getDataDestination().getProperty("path");
            return path != null && new File(path).exists();
        }
    }
}
