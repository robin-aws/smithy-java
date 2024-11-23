/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.client.inmemory;

import java.net.http.HttpClient;
import java.util.concurrent.CompletableFuture;

import software.amazon.smithy.java.client.core.ClientTransport;
import software.amazon.smithy.java.client.core.ClientTransportFactory;
import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.java.inmemory.api.InMemoryRequest;
import software.amazon.smithy.java.inmemory.api.InMemoryResponse;
import software.amazon.smithy.java.logging.InternalLogger;

/**
 */
public class JavaInMemoryClientTransport implements ClientTransport<InMemoryRequest, InMemoryResponse> {

    private static final InternalLogger LOGGER = InternalLogger.getLogger(JavaInMemoryClientTransport.class);
    private final HttpClient client;

    public JavaInMemoryClientTransport() {
        this(HttpClient.newHttpClient());
    }

    /**
     * @param client Java client to use.
     */
    public JavaInMemoryClientTransport(HttpClient client) {
        this.client = client;
    }

    @Override
    public Class<InMemoryRequest> requestClass() {
        return InMemoryRequest.class;
    }

    @Override
    public Class<InMemoryResponse> responseClass() {
        return InMemoryResponse.class;
    }

    @Override
    public CompletableFuture<InMemoryResponse> send(Context context, InMemoryRequest request) {
        throw new UnsupportedOperationException();
    }

    public static final class Factory implements ClientTransportFactory<InMemoryRequest, InMemoryResponse> {

        @Override
        public String name() {
            return "in-memory-java";
        }

        // TODO: Determine what configuration is actually needed.
        @Override
        public JavaInMemoryClientTransport createTransport(Document node) {
            return new JavaInMemoryClientTransport();
        }

        @Override
        public Class<InMemoryRequest> requestClass() {
            return InMemoryRequest.class;
        }

        @Override
        public Class<InMemoryResponse> responseClass() {
            return InMemoryResponse.class;
        }
    }
}
