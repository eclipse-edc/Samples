/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.sample.runtime;

import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SeedVaultExtension implements ServiceExtension {

    @Inject
    private Vault vault;

    @Override
    public void initialize(ServiceExtensionContext context) {
        loadToVault("cert.pem", "public-key");
        loadToVault("key.pem", "private-key");
    }

    private void loadToVault(String resource, String secretKey) {
        var path = getClass().getClassLoader().getResource(resource).getPath();
        try {
            var publicKey = Files.readString(Path.of(path));
            vault.storeSecret(secretKey, publicKey);
        } catch (IOException e) {
            throw new EdcException("Cannot load public key from " + path);
        }
    }
}
