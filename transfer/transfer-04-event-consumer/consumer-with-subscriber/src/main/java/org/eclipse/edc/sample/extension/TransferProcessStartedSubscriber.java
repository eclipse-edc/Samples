/*
 *  Copyright (c) 2026 Think-it GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Think-it GmbH - initial API and implementation
 *
 */

package org.eclipse.edc.sample.extension;

import org.eclipse.edc.spi.event.Event;
import org.eclipse.edc.spi.event.EventEnvelope;
import org.eclipse.edc.spi.event.EventSubscriber;
import org.eclipse.edc.spi.monitor.Monitor;

class TransferProcessStartedSubscriber implements EventSubscriber {
    private final Monitor monitor;

    TransferProcessStartedSubscriber(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public <E extends Event> void on(EventEnvelope<E> event) {
        monitor.info("TransferProcessStarted event has been emitted.");
        // do something after transfer has been started
    }
}
