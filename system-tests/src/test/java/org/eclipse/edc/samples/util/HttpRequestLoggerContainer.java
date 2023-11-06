package org.eclipse.edc.samples.util;

import org.testcontainers.containers.*;
import org.testcontainers.images.builder.*;

public class HttpRequestLoggerContainer extends GenericContainer<HttpRequestLoggerContainer> {

    public HttpRequestLoggerContainer(ImageFromDockerfile dockerImage) {
        super(dockerImage);
    }
}
