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
