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
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.types.TypeManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CatalogNodeDirectoryExtension implements ServiceExtension {
    @Inject
    private TypeManager typeManager;

    @Provider 
    public TargetNodeDirectory federatedCacheNodeDirectory() {
        String participantsFilePath = "participants.json";

        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream participantFileInputStream = classLoader.getResourceAsStream(participantsFilePath)) {
            if (participantFileInputStream == null) {
                throw new RuntimeException("Participant list file does not exist: " + participantsFilePath);
            }

            String participantFileContent = new String(participantFileInputStream.readAllBytes(), StandardCharsets.UTF_8);

            return new CatalogNodeDirectory(typeManager.getMapper(), participantFileContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read and map participant list file: " + participantsFilePath, e);
        }
    }
}
