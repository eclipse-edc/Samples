/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.sample.extension;

import org.eclipse.edc.connector.controlplane.asset.spi.event.AssetCreated;
import org.eclipse.edc.spi.event.Event;
import org.eclipse.edc.spi.event.EventEnvelope;
import org.eclipse.edc.spi.event.EventSubscriber;
import org.eclipse.edc.spi.monitor.Monitor;

class AssetCreatedSubscriber implements EventSubscriber {

    private final Monitor monitor;

    AssetCreatedSubscriber(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public <E extends Event> void on(EventEnvelope<E> envelope) {
        if (envelope.getPayload() instanceof AssetCreated event) {
            monitor.info("Asset created: " + event.getAssetId());
        }
    }

}
