/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class ReceiverHandler implements HttpHandler {

    /**
     * This method just prints the request body to the console and returns a 200 OK response.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Request Body: " + new String(exchange.getRequestBody().readAllBytes()));
        exchange.sendResponseHeaders(200, 0);
    }
}
