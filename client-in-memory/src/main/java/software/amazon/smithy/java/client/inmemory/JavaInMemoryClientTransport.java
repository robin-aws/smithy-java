/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.client.inmemory;

import software.amazon.smithy.java.client.core.ClientTransport;
import software.amazon.smithy.java.client.core.ClientTransportFactory;
import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.java.logging.InternalLogger;
import software.amazon.smithy.java.server.core.InMemoryRequest;
import software.amazon.smithy.java.server.core.InMemoryResponse;
import software.amazon.smithy.java.server.core.InMemoryServer;

import java.util.concurrent.CompletableFuture;

/**
 */
// TODO: Drop the "Java" prefix, I copied that from JavaHttpClientTransport
// but there it refers to "the built-in Java HttpCLient"
public class JavaInMemoryClientTransport implements ClientTransport<InMemoryRequest, InMemoryResponse> {

    private static final InternalLogger LOGGER = InternalLogger.getLogger(JavaInMemoryClientTransport.class);

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
        // TODO: Works, but may not be the ideal minimal FFI signature,
        // especially the typed context map which could have arbitrary types in it.
        // TODO: Should have a registry analogous to DNS to map URI prefixes to servers instead
        return InMemoryServer.SERVER.handle(context, request);
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
