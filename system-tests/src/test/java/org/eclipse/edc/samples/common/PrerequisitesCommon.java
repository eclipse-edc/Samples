/*
 *  Copyright (c) 2023 Mercedes-Benz Tech Innovation GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Mercedes-Benz Tech Innovation GmbH - Initial implementation
 *
 */

package org.eclipse.edc.samples.common;

import org.eclipse.edc.junit.extensions.EmbeddedRuntime;
import org.eclipse.edc.junit.extensions.RuntimeExtension;
import org.eclipse.edc.junit.extensions.RuntimePerClassExtension;
import org.eclipse.edc.samples.util.ConfigPropertiesLoader;
import org.eclipse.edc.spi.system.configuration.ConfigFactory;

import java.util.Map;

import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;

public class PrerequisitesCommon {
    public static final String API_KEY_HEADER_KEY = "X-Api-Key";
    public static final String API_KEY_HEADER_VALUE = "password";
    public static final String PROVIDER_MANAGEMENT_URL = "http://localhost:19193/management";
    public static final String CONSUMER_MANAGEMENT_URL = "http://localhost:29193/management";

    private static final String CONNECTOR_MODULE_PATH = ":transfer:transfer-00-prerequisites:connector";
    private static final String PROVIDER = "provider";
    private static final String CONSUMER = "consumer";
    private static final String EDC_KEYSTORE = "edc.keystore";
    private static final String EDC_KEYSTORE_PASSWORD = "edc.keystore.password";

    private static final String CERT_PFX_FILE_PATH = "transfer/transfer-00-prerequisites/resources/certs/cert.pfx";
    private static final String KEYSTORE_PASSWORD = "123456";
    private static final String PROVIDER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-00-prerequisites/resources/configuration/provider-configuration.properties";
    private static final String CONSUMER_CONFIG_PROPERTIES_FILE_PATH = "transfer/transfer-00-prerequisites/resources/configuration/consumer-configuration.properties";

    public static RuntimeExtension getProvider() {
        return getProvider(CONNECTOR_MODULE_PATH, PROVIDER_CONFIG_PROPERTIES_FILE_PATH);
    }

    public static RuntimeExtension getProvider(String modulePath, String configPath) {
        return getConnector(modulePath, PROVIDER, configPath);
    }

    public static RuntimeExtension getConsumer() {
        return getConsumer(CONNECTOR_MODULE_PATH);
    }

    public static RuntimeExtension getConsumer(String modulePath) {
        return getConnector(modulePath, CONSUMER, CONSUMER_CONFIG_PROPERTIES_FILE_PATH);
    }

    private static RuntimeExtension getConnector(
            String modulePath,
            String moduleName,
            String configPropertiesFilePath
    ) {
        return new RuntimePerClassExtension(new EmbeddedRuntime(moduleName, modulePath)
                .configurationProvider(ConfigPropertiesLoader.fromPropertiesFile(configPropertiesFilePath))
                .configurationProvider(() -> ConfigFactory.fromMap(Map.of(
                    EDC_KEYSTORE, getFileFromRelativePath(CERT_PFX_FILE_PATH).getAbsolutePath(),
                    EDC_KEYSTORE_PASSWORD, KEYSTORE_PASSWORD))
                )
        );
    }
}
