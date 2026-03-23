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

import org.eclipse.edc.connector.controlplane.transfer.spi.observe.TransferProcessListener;
import org.eclipse.edc.connector.controlplane.transfer.spi.observe.TransferProcessStartedData;
import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcess;
import org.eclipse.edc.spi.monitor.Monitor;

public class TransferProcessStartedListener implements TransferProcessListener {

    private final Monitor monitor;

    public TransferProcessStartedListener(Monitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Callback invoked by the EDC framework when a transfer is started.
     *
     * @param process        the transfer process that has been started.
     * @param additionalData additional data associated with the started transfer process.
     */
    @Override
    public void started(final TransferProcess process, final TransferProcessStartedData additionalData) {
        monitor.info("TransferProcessStartedListener received STARTED event");
        // do something meaningful when transfer starts
    }
}
