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
import org.eclipse.edc.connector.controlplane.transfer.spi.types.TransferProcess;
import org.eclipse.edc.spi.monitor.Monitor;

public class TransferProcessStartedListener implements TransferProcessListener {

    private final Monitor monitor;

    public TransferProcessStartedListener(Monitor monitor) {
        this.monitor = monitor;
    }

    /**
     * Callback invoked by the EDC framework when a transfer is about to be completed.
     *
     * @param process the transfer process that is about to be completed.
     */
    @Override
    public void preStarted(final TransferProcess process) {
        monitor.info("TransferProcessStartedListener received STARTED event");
        // do something meaningful before transfer start
    }
}
