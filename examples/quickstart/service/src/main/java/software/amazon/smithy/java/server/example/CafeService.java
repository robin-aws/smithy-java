/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.server.example;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import software.amazon.smithy.java.server.Server;
import software.amazon.smithy.java.server.example.service.CoffeeShop;

public class CafeService implements Runnable {
    static final URI endpoint = URI.create("http://localhost:8888");

    public static void main(String... args) {
        new CafeService().run();
    }

    @Override
    public void run() {
        Server server = Server.builder()
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
