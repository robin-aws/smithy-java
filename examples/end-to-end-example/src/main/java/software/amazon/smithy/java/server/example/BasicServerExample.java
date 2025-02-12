/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.server.example;

import java.util.concurrent.ExecutionException;
import software.amazon.smithy.java.core.endpoint.Endpoint;
import software.amazon.smithy.java.example.service.CoffeeShop;
import software.amazon.smithy.java.server.Server;

public class BasicServerExample implements Runnable {
    // Existing endpoint for this example
    static final Endpoint endpoint = Endpoint.create("http://localhost:8888");
    static final String serverProviderName = "smithy-java-netty-server";

    // For in-memory transports
    // TODO: Endpoint probably needs to identify a software package as well.
    // Could use a mvn: channel URI? https://www.iana.org/assignments/uri-schemes/prov/mvn
//    static final Endpoint endpoint = Endpoint.create(
//        "inmemory://java/software.amazon.smithy.java.server.core.InMemoryServer.SERVER");
//    static final String serverProviderName = "smithy-java-in-memory-server";

    // For HTTP transports over unix domain sockets
//    static final Endpoint endpoint = Endpoint.builder()
//            .uri("http://localhost/")
//            .channelUri("unix:%s/.aws/localservices/beer/%s".formatted(
//                    System.getProperty("user.home"), ProcessHandle.current().pid()))
//            .build();
//    static final String serverProviderName = "smithy-java-netty-server";

    @Override
    public void run() {
        Server server = Server.builder(serverProviderName)
            .endpoints(endpoint)
            .addService(
                CoffeeShop.builder()
                    .addCreateOrderOperation(new CreateOrder())
                    .addGetMenuOperation(new GetMenu())
                    .addGetOrderOperation(new GetOrder())
                    .build()
            )
            .build();
        System.out.println("Starting server...");
        server.start();
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Stopping server...");
            try {
                server.shutdown().get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
