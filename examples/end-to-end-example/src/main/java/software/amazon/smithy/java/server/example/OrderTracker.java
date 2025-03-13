/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.server.example;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import software.amazon.smithy.java.client.core.endpoint.EndpointResolver;
import software.amazon.smithy.java.core.endpoint.Endpoint;
import software.amazon.smithy.java.example.callbacks.client.CoffeeShopCallbacksClient;
import software.amazon.smithy.java.example.callbacks.model.CoffeeType;
import software.amazon.smithy.java.example.callbacks.model.NotifyCompletedInput;
import software.amazon.smithy.java.example.model.OrderStatus;

/**
 * This class is a stand-in for a database.
 */
final class OrderTracker {
    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private static final Map<UUID, Order> ORDERS = new ConcurrentHashMap<>();

    public static void putOrder(Order order) {
        ORDERS.put(order.id(), order);

        // Start a process to complete the order in the background.
        executor.schedule(
            () -> completeOrder(new Order(order.id(), order.type(), OrderStatus.COMPLETED, order.callbackEndpoint(), order.callbackId())),
            5,
            TimeUnit.SECONDS
        );
    }

    public static Order getOrderById(UUID id) {
        return ORDERS.get(id);
    }

    private static void completeOrder(Order order) {
        ORDERS.put(order.id(), order);

        if (order.callbackId() != null) {
            EndpointResolver endpointResolver;
            if (order.callbackEndpoint() != null) {
                endpointResolver = ResolvedEndpointResolver.staticResolvedEndpoint(order.callbackEndpoint());
            } else {
                endpointResolver = EndpointResolver.staticEndpoint(
                        LocalService.defaultInMemoryEndpoint("coffeeshopcallbacks"));
            }
            CoffeeShopCallbacksClient client = CoffeeShopCallbacksClient.builder()
                    .endpointResolver(endpointResolver)
                    .build();
            try {
                client.notifyCompleted(NotifyCompletedInput.builder()
                        .callbackId(order.callbackId())
                        .orderId(order.id().toString())
                        .coffeeType(CoffeeType.from(order.type().value()))
                        .build());
            } catch (Exception e) {
                // Would be logged in a proper solution,
                // likely even by the generated client/server code itself.
                // This is just to help me debug things.
                System.err.println(e);
            }
        }
    }
}
