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

package org.eclipse.edc.samples.util;

import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;

public class HttpRequestLoggerUtil {

    private static final String HTTP_REQUEST_LOGGER_DOCKER_COMPOSE_FILE_PATH = "util/http-request-logger/docker-compose.yaml";

    private static final String HTTP_REQUEST_LOGGER = "http-request-logger";

    public static DockerComposeContainer<?> getHttpRequestLoggerContainer() {
        return new DockerComposeContainer<>(getFileFromRelativePath(HTTP_REQUEST_LOGGER_DOCKER_COMPOSE_FILE_PATH))
                .withLocalCompose(true)
                .waitingFor(HttpRequestLoggerUtil.HTTP_REQUEST_LOGGER, Wait.forLogMessage(".*started.*", 1));
    }

    public static DockerComposeContainer<?> getHttpRequestLoggerContainer(ToStringConsumer toStringConsumer) {
        return getHttpRequestLoggerContainer()
                .withLogConsumer(HTTP_REQUEST_LOGGER, toStringConsumer);
    }

}
