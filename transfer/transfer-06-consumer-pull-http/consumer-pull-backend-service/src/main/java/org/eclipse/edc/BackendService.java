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

package org.eclipse.edc;

import com.sun.net.httpserver.HttpServer;
import org.eclipse.edc.handler.ReceiverHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;

public class BackendService {

    static final String HTTP_PORT = "server.port";

    public static void main(String[] args) {
        int port = Integer.parseInt(Optional.ofNullable(System.getenv(HTTP_PORT)).orElse("4000"));
        var server = createHttpServer(port);
        server.createContext("/receiver", new ReceiverHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("server started at " + port);
    }
    
    private static HttpServer createHttpServer(int port) {
        try {
            return HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            throw new RuntimeException("Unable to start server at port " + port, e);
        }
    }
}
