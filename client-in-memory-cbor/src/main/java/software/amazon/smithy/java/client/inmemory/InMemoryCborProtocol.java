/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.client.inmemory;

import software.amazon.smithy.java.cbor.Rpcv2CborCodec;
import software.amazon.smithy.java.client.core.ClientProtocol;
import software.amazon.smithy.java.client.core.ClientProtocolFactory;
import software.amazon.smithy.java.client.core.ProtocolSettings;
import software.amazon.smithy.java.client.core.endpoint.Endpoint;
import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.core.schema.ApiOperation;
import software.amazon.smithy.java.core.schema.SerializableStruct;
import software.amazon.smithy.java.core.serde.Codec;
import software.amazon.smithy.java.core.serde.TypeRegistry;
import software.amazon.smithy.java.io.ByteBufferOutputStream;
import software.amazon.smithy.java.io.datastream.DataStream;
import software.amazon.smithy.java.server.core.InMemoryDataStreamRequest;
import software.amazon.smithy.java.server.core.InMemoryDataStreamResponse;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.protocol.traits.InMemoryCborTrait;
import software.amazon.smithy.protocol.traits.Rpcv2CborTrait;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class InMemoryCborProtocol extends InMemoryClientProtocol<InMemoryDataStreamRequest, InMemoryDataStreamResponse> {
    private static final Codec CBOR_CODEC = Rpcv2CborCodec.builder().build();
    private static final List<String> SMITHY_PROTOCOL = List.of("in-memory-v1-cbor");

    private final ShapeId service;

    public InMemoryCborProtocol(ShapeId service) {
        super(InMemoryCborTrait.ID.toString());
        this.service = service;
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
    public <I extends SerializableStruct, O extends SerializableStruct> InMemoryDataStreamRequest createRequest(
            ApiOperation<I, O> operation,
            I input,
            Context context,
            URI endpoint
    ) {
        var sink = new ByteBufferOutputStream();
        try (var serializer = CBOR_CODEC.createSerializer(sink)) {
            input.serialize(serializer);
        }
        var body = DataStream.ofByteBuffer(sink.toByteBuffer(), "application/cbor");
        return new InMemoryDataStreamRequest(body);
    }

    @Override
    public InMemoryDataStreamRequest setServiceEndpoint(InMemoryDataStreamRequest request, Endpoint endpoint) {
        // No-op for now - it seems to make sense for "this process"
        // to be the only valid endpoint, as a singleton.
        // It might be safer to explicitly define that dummy endpoint and check it though.
        return request;
    }

    @Override
    public <I extends SerializableStruct, O extends SerializableStruct> CompletableFuture<O> deserializeResponse(
            ApiOperation<I, O> operation,
            Context context,
            TypeRegistry typeRegistry,
            InMemoryDataStreamRequest request,
            InMemoryDataStreamResponse response
    ) {
        var builder = operation.outputBuilder();
        var content = response.getSerializedValue();
        if (content.contentLength() == 0) {
            return CompletableFuture.completedFuture(builder.build());
        }

        return content.asByteBuffer()
                .thenApply(bytes -> CBOR_CODEC.deserializeShape(bytes, builder))
                .toCompletableFuture();
    }

    public static final class Factory implements ClientProtocolFactory<Rpcv2CborTrait> {
        @Override
        public ShapeId id() {
            return Rpcv2CborTrait.ID;
        }

        @Override
        public ClientProtocol<?, ?> createProtocol(ProtocolSettings settings, Rpcv2CborTrait trait) {
            return new InMemoryCborProtocol(
                    Objects.requireNonNull(
                            settings.service(),
                            "service is a required protocol setting"
                    )
            );
        }
    }
}
