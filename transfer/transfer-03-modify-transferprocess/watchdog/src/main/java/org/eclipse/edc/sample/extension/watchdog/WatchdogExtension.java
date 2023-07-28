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
 *       Microsoft Corporation - Initial implementation
 *
 */

package org.eclipse.edc.sample.extension.watchdog;

import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.time.Clock;

public class WatchdogExtension implements ServiceExtension {

    @Inject
    private TransferProcessStore store;

    @Inject
    private Clock clock;

    private Watchdog watchDog;

    @Override
    public void initialize(ServiceExtensionContext context) {
        watchDog = new Watchdog(context.getMonitor(), store, clock);
    }

    @Override
    public void start() {
        watchDog.start();
    }

    @Override
    public void shutdown() {
        watchDog.stop();
    }
}
