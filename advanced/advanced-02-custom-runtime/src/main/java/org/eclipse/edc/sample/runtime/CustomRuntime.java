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

package org.eclipse.edc.sample.runtime;

import org.eclipse.edc.boot.system.DefaultServiceExtensionContext;
import org.eclipse.edc.boot.system.runtime.BaseRuntime;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.ServiceExtensionContext;
import org.eclipse.edc.spi.system.configuration.Config;
import org.jetbrains.annotations.NotNull;

public class CustomRuntime extends BaseRuntime {

    /**
     * The {@code main} method must be re-implemented, otherwise {@link BaseRuntime#main(String[])} would be called, which would
     * instantiate the {@code BaseRuntime}.
     */
    public static void main(String[] args) {
        new CustomRuntime().boot(true);
    }

    @Override
    protected @NotNull ServiceExtensionContext createContext(Monitor monitor, Config config) {
        //override the default service extension context with a super customized one
        return new SuperCustomExtensionContext(monitor, config);
    }

    @Override
    public void shutdown() {
        super.shutdown();

        //this is the custom part here:
        monitor.info(" CUSTOM RUNTIME SHUTDOWN ! ");
    }

    private static class SuperCustomExtensionContext extends DefaultServiceExtensionContext {
        SuperCustomExtensionContext(Monitor monitor, Config config) {
            super(monitor, config);
        }
    }
}
