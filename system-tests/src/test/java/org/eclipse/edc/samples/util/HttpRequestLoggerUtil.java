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

import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.util.List;

import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;

public class HttpRequestLoggerUtil {

    private static final String HTTP_REQUEST_LOGGER_DOCKERFILE_PATH = "util/http-request-logger/Dockerfile";
    private static final String PORT_BINDING = "4000:4000";

    public static HttpRequestLoggerContainer getHttpRequestLoggerContainer() {
        var container = new HttpRequestLoggerContainer(getDockerImage());
        container.setPortBindings(List.of(PORT_BINDING));
        return container;
    }

    public static HttpRequestLoggerContainer getHttpRequestLoggerContainer(ToStringConsumer toStringConsumer) {
        return getHttpRequestLoggerContainer()
                .withLogConsumer(toStringConsumer);
    }

    private static ImageFromDockerfile getDockerImage() {
        return new ImageFromDockerfile()
                .withDockerfile(getFileFromRelativePath(HTTP_REQUEST_LOGGER_DOCKERFILE_PATH).toPath());
    }
}
