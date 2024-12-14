/*
 *  Copyright (c) 2024 Fraunhofer-Gesellschaft
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer-Gesellschaft - initial API and implementation
 *
 */

package org.eclipse.edc.sample.extension.fc;

import org.eclipse.edc.crawler.spi.TargetNodeDirectory;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.io.File;

public class CatalogNodeDirectoryExtension implements ServiceExtension {

    private File participantListFile;

    @Override
    public void initialize(ServiceExtensionContext context) {
        var participantsFilePath = "federated-catalog/fc-03-static-node-directory/target-node-resolver/resources/participants.json";

        participantListFile = new File(participantsFilePath).getAbsoluteFile();
        if (!participantListFile.exists()) {
            context.getMonitor().warning("Path '%s' does not exist.".formatted(participantsFilePath));
        }
    }

    @Provider 
    public TargetNodeDirectory federatedCacheNodeDirectory() {
        return new CatalogNodeDirectory(participantListFile);
    }

}
