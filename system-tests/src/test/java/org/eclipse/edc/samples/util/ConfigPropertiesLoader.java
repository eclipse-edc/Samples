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

package org.eclipse.edc.samples.util;

import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.system.configuration.Config;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.function.Supplier;

import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;

/**
 * Load config from properties file
 */
public class ConfigPropertiesLoader implements Supplier<Config> {

    private final String path;

    public static ConfigPropertiesLoader fromPropertiesFile(String path) {
        return new ConfigPropertiesLoader(path);
    }

    public ConfigPropertiesLoader(String path) {
        this.path = path;
    }

    @Override
    public Config get() {
        var properties = new Properties();
        var filePath = getFileFromRelativePath(path).toURI();
        try (var stream = filePath.toURL().openStream()) {
            properties.load(stream);
        } catch (IOException e) {
            throw new EdcException("Cannot load properties file " + filePath, e);
        }
        return ConfigFactory.fromProperties(properties);
    }
}
