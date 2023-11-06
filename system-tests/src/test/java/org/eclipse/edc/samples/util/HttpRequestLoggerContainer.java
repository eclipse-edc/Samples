package org.eclipse.edc.samples.util;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class HttpRequestLoggerContainer extends GenericContainer<HttpRequestLoggerContainer> {

    public HttpRequestLoggerContainer(ImageFromDockerfile dockerImage) {
        super(dockerImage);
    }
}
