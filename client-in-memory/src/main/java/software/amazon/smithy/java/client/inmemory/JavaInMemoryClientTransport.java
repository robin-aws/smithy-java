/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.client.inmemory;

import software.amazon.smithy.java.client.core.ClientTransport;
import software.amazon.smithy.java.client.core.ClientTransportFactory;
import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.java.inmemory.api.InMemoryRequest;
import software.amazon.smithy.java.inmemory.api.InMemoryResponse;
import software.amazon.smithy.java.logging.InternalLogger;
import software.amazon.smithy.java.server.core.DefaultJob;
import software.amazon.smithy.java.server.core.InMemoryJob;
import software.amazon.smithy.java.server.core.InMemoryServerRequest;
import software.amazon.smithy.java.server.core.InMemoryServerResponse;
import software.amazon.smithy.java.server.core.Orchestrator;
import software.amazon.smithy.java.server.core.ProtocolResolver;
import software.amazon.smithy.java.server.core.ServiceProtocolResolutionRequest;

import java.util.concurrent.CompletableFuture;

/**
 */
public class JavaInMemoryClientTransport implements ClientTransport<InMemoryServerRequest, InMemoryServerResponse> {

    private static final InternalLogger LOGGER = InternalLogger.getLogger(JavaInMemoryClientTransport.class);

    private final Orchestrator orchestrator;
    private final ProtocolResolver resolver;

    /**
     */
    public JavaInMemoryClientTransport(Orchestrator orchestrator, ProtocolResolver resolver) {
        this.orchestrator = orchestrator;
        this.resolver = resolver;
    }

    @Override
    public Class<InMemoryServerRequest> requestClass() {
        return InMemoryServerRequest.class;
    }

    @Override
    public Class<InMemoryServerResponse> responseClass() {
        return InMemoryServerResponse.class;
    }

    @Override
    public CompletableFuture<InMemoryServerResponse> send(Context context, InMemoryServerRequest request) {
        var resolutionResult = resolver.resolve(
                // TODO: generalize the resolution API, or encode information as a uri, or leverage the context
                new ServiceProtocolResolutionRequest(null, null, context, null)
        );
        var response = new InMemoryServerResponse();
        var job = new InMemoryJob(resolutionResult.operation(), resolutionResult.protocol(), request, response);
        return orchestrator.enqueue(job)
                           .thenCompose(r -> CompletableFuture.completedFuture(response));
    }

    public static final class Factory implements ClientTransportFactory<InMemoryServerRequest, InMemoryServerResponse> {

        @Override
        public String name() {
            return "in-memory-java";
        }

        // TODO: Determine what configuration is actually needed.
        @Override
        public JavaInMemoryClientTransport createTransport(Document node) {
            // TODO: Probably need an SPI for discovering orchestrators and resolvers
            return new JavaInMemoryClientTransport(null, null);
        }

        @Override
        public Class<InMemoryServerRequest> requestClass() {
            return InMemoryServerRequest.class;
        }

        @Override
        public Class<InMemoryServerResponse> responseClass() {
            return InMemoryServerResponse.class;
        }
    }
}
