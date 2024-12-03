/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.server.example;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import software.amazon.smithy.java.inmemory.example.service.CoffeeShop;
import software.amazon.smithy.java.server.Server;
import software.amazon.smithy.java.server.core.InMemoryServerBuilder;

public class BasicServerExample implements Runnable {
    static final URI endpoint = URI.create("inmemory:///");

    @Override
    public void run() {
        Server server = new InMemoryServerBuilder()
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
