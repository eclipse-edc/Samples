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
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.query.Criterion;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.time.Instant.ofEpochMilli;
import static org.eclipse.edc.connector.transfer.spi.types.TransferProcessStates.STARTED;

public class Watchdog {

    private final Monitor monitor;
    private final TransferProcessStore store;
    private final Clock clock;
    private ScheduledExecutorService executor;

    public Watchdog(Monitor monitor, TransferProcessStore store, Clock clock) {
        this.monitor = monitor;
        this.store = store;
        this.clock = clock;
    }

    public void start() {
        executor = Executors.newSingleThreadScheduledExecutor();
        // run every 10 minutes, no initial delay
        executor.scheduleAtFixedRate(this::check, 10, 10, TimeUnit.SECONDS);
    }

    public void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    private void check() {
        monitor.info("Running watchdog");

        var states = store.nextNotLeased(3, new Criterion("state", "=", STARTED.code()));
        states.stream().filter(tp -> isExpired(tp.getStateTimestamp(), Duration.ofSeconds(10)))
                .forEach(tp -> {
                    monitor.info(format("will retire TP with id [%s] due to timeout", tp.getId()));

                    tp.transitionTerminating("timeout by watchdog");
                    store.save(tp);
                });
    }

    private boolean isExpired(long stateTimestamp, Duration maxAge) {
        return ofEpochMilli(stateTimestamp).isBefore(clock.instant().minus(maxAge));
    }
}
