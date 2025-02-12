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
 *       Mercedes-Benz Tech Innovation GmbH - initial implementation
 *
 */

package org.eclipse.edc.samples.util;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import static org.eclipse.edc.samples.common.FileTransferCommon.getFileFromRelativePath;

public class HttpRequestLoggerContainer extends GenericContainer<HttpRequestLoggerContainer> {

    private static final String HTTP_REQUEST_LOGGER_DOCKERFILE_PATH = "util/http-request-logger/Dockerfile";
    private static final ImageFromDockerfile IMAGE_FROM_DOCKERFILE = new ImageFromDockerfile()
            .withDockerfile(getFileFromRelativePath(HTTP_REQUEST_LOGGER_DOCKERFILE_PATH).toPath());
    private final ToStringConsumer toStringConsumer;

    public HttpRequestLoggerContainer() {
        super(IMAGE_FROM_DOCKERFILE);
        this.toStringConsumer =  new HttpRequestLoggerConsumer();
        this.withLogConsumer(toStringConsumer)
                .withExposedPorts(4000);
    }

    public String getLog() {
        return this.toStringConsumer.toUtf8String();
    }

    public int getPort() {
        return this.getFirstMappedPort();
    }
}
