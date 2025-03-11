/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.server.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.java.client.core.endpoint.EndpointResolver;
import software.amazon.smithy.java.core.endpoint.Endpoint;
import software.amazon.smithy.java.example.callbacks.model.NotifyCompletedInput;
import software.amazon.smithy.java.example.callbacks.model.NotifyCompletedOutput;
import software.amazon.smithy.java.example.callbacks.service.CoffeeShopCallbacks;
import software.amazon.smithy.java.example.client.CoffeeShopClient;
import software.amazon.smithy.java.example.model.CoffeeType;
import software.amazon.smithy.java.example.model.CreateOrderInput;
import software.amazon.smithy.java.example.model.GetMenuInput;
import software.amazon.smithy.java.example.model.GetOrderInput;
import software.amazon.smithy.java.example.model.OrderNotFound;
import software.amazon.smithy.java.example.model.OrderStatus;
import software.amazon.smithy.java.example.model.ResolvedEndpoint;
import software.amazon.smithy.java.server.RequestContext;
import software.amazon.smithy.java.server.Server;
import software.amazon.smithy.java.server.core.InMemoryServer;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoundTripTests {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    // TODO: In an actual in-memory local service (a.k.a. library)
    // the library would start the server for you on initialization somehow.
    // The library user would just make requests through the client.
    @BeforeAll
    public static void setup() throws InterruptedException {
        var server = new BasicServerExample();
        executor.execute(server);
        // Wait for server to start
        while (!serverListening(BasicServerExample.endpoint)) {
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public static boolean serverListening(Endpoint endpoint) {
        final var uri = endpoint.uri();
        final var channelUri = endpoint.channelUri();
        if (uri.getScheme().equals("inmemory")) {
            return InMemoryServer.SERVER != null;
        } else if (channelUri.getScheme().equals("unix")) {
            // TODO: Figure out the equivalent. Spinning up Netty is non-trivial.
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        try (Socket ignored = new Socket(uri.getHost(), uri.getPort())) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Test
    void executesCorrectly() throws InterruptedException {
        CoffeeShopClient client = CoffeeShopClient.builder()
            .endpointResolver(EndpointResolver.staticEndpoint(BasicServerExample.endpoint))
            .build();

        var menu = client.getMenu(GetMenuInput.builder().build());
        var hasEspresso = menu.items().stream().anyMatch(item -> item.typeMember().equals(CoffeeType.ESPRESSO));
        assertTrue(hasEspresso);

        var createRequest = CreateOrderInput.builder().coffeeType(CoffeeType.COLD_BREW).build();
        var createResponse = client.createOrder(createRequest);
        assertEquals(CoffeeType.COLD_BREW, createResponse.coffeeType());
        System.out.println("Created request with id = " + createResponse.id());

        var getRequest = GetOrderInput.builder().id(createResponse.id()).build();
        var getResponse1 = client.getOrder(getRequest);
        assertEquals(getResponse1.status(), OrderStatus.IN_PROGRESS);

        // Give order some time to complete
        System.out.println("Waiting for order to complete....");
        TimeUnit.SECONDS.sleep(5);

        var getResponse2 = client.getOrder(getRequest);
        assertEquals(getResponse2.status(), OrderStatus.COMPLETED);
        System.out.println("Completed Order :" + getResponse2);
    }

    @Test
    void executesCorrectlyWithCallback() throws InterruptedException {
        CoffeeShopClient client = CoffeeShopClient.builder()
                .endpointResolver(EndpointResolver.staticEndpoint(BasicServerExample.endpoint))
                .build();

        var menu = client.getMenu(GetMenuInput.builder().build());
        var hasEspresso = menu.items().stream().anyMatch(item -> item.typeMember().equals(CoffeeType.ESPRESSO));
        assertTrue(hasEspresso);

        AtomicReference<String> completedOrder = new AtomicReference<>();
        var createRequest = CreateOrderInput.builder()
                .coffeeType(CoffeeType.COLD_BREW)
                // For some local services, especially in-memory ones,
                // the callback endpoint could be configured once
                // in an Initialize operation.
                .callbackEndpoint(CoffeeCallbacks.getEndpoint())
                .callbackId(CoffeeCallbacks.registerCallback((input, context) -> {
                    completedOrder.set(input.orderId());
                    return CompletableFuture.completedFuture(NotifyCompletedOutput.builder().starRating(4).build());
                }))
                .build();

        var createResponse = client.createOrder(createRequest);
        assertEquals(CoffeeType.COLD_BREW, createResponse.coffeeType());
        System.out.println("Created request with id = " + createResponse.id());

        var getRequest = GetOrderInput.builder().id(createResponse.id()).build();
        var getResponse1 = client.getOrder(getRequest);
        assertEquals(getResponse1.status(), OrderStatus.IN_PROGRESS);

        // Give order some time to complete
        System.out.println("Waiting for order to complete....");
        TimeUnit.SECONDS.sleep(10);

        assertEquals(createResponse.id(), completedOrder.get());
    }

    @Test
    void errorsOutIfOrderDoesNotExist() throws InterruptedException {
        CoffeeShopClient client = CoffeeShopClient.builder()
            .endpointResolver(EndpointResolver.staticEndpoint(BasicServerExample.endpoint))
            .build();

        var getRequest = GetOrderInput.builder().id(UUID.randomUUID().toString()).build();
        var orderNotFound = assertThrows(OrderNotFound.class, () -> client.getOrder(getRequest));
        assertEquals(orderNotFound.orderId(), getRequest.id());
    }

    @AfterAll
    public static void teardown() {
        executor.shutdownNow();
    }
}
