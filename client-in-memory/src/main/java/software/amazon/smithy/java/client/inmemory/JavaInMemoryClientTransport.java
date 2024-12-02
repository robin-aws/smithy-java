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
import software.amazon.smithy.java.server.core.InMemoryJob;
import software.amazon.smithy.java.server.core.InMemoryDataStreamRequest;
import software.amazon.smithy.java.server.core.InMemoryDataStreamResponse;
import software.amazon.smithy.java.server.core.Orchestrator;
import software.amazon.smithy.java.server.core.ProtocolResolver;
import software.amazon.smithy.java.server.core.ServiceProtocolResolutionRequest;

import java.util.concurrent.CompletableFuture;

/**
 */
public class JavaInMemoryClientTransport implements ClientTransport<InMemoryDataStreamRequest, InMemoryDataStreamResponse> {

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
    public Class<InMemoryDataStreamRequest> requestClass() {
        return InMemoryDataStreamRequest.class;
    }

    @Override
    public Class<InMemoryDataStreamResponse> responseClass() {
        return InMemoryDataStreamResponse.class;
    }

    @Override
    public CompletableFuture<InMemoryDataStreamResponse> send(Context context, InMemoryDataStreamRequest request) {
        var resolutionResult = resolver.resolve(
                new ServiceProtocolResolutionRequest(request.getUri(), null, context, null)
        );
        var response = new InMemoryDataStreamResponse();
        var job = new InMemoryJob(resolutionResult.operation(), resolutionResult.protocol(), request, response);
        return orchestrator.enqueue(job)
                           .thenCompose(r -> CompletableFuture.completedFuture(response));
    }

    public static final class Factory implements ClientTransportFactory<InMemoryDataStreamRequest, InMemoryDataStreamResponse> {

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
        public Class<InMemoryDataStreamRequest> requestClass() {
            return InMemoryDataStreamRequest.class;
        }

        @Override
        public Class<InMemoryDataStreamResponse> responseClass() {
            return InMemoryDataStreamResponse.class;
        }
    }
}
