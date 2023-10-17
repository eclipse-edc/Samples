package org.eclipse.edc.samples.transfer;

import org.testcontainers.containers.output.ToStringConsumer;

import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class HttpRequestLoggerConsumer extends ToStringConsumer {

    private static final String REGEX_FORMAT = "(?<=\"%s\":\")[^\"]*";

    public String getJsonValue(String key) {
        var pattern = Pattern.compile(format(REGEX_FORMAT, key));
        var matcher = pattern.matcher(toUtf8String());
        if (matcher.find()) {
            return matcher.group();
        }
        return EMPTY;
    }
}
