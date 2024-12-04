package software.amazon.smithy.java.client.inmemory;

import software.amazon.smithy.java.client.core.ClientProtocol;
import software.amazon.smithy.java.client.core.ClientProtocolFactory;
import software.amazon.smithy.java.client.core.ProtocolSettings;
import software.amazon.smithy.java.client.core.endpoint.Endpoint;
import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.core.schema.ApiOperation;
import software.amazon.smithy.java.core.schema.SerializableStruct;
import software.amazon.smithy.java.core.serde.ConsumerSerializer;
import software.amazon.smithy.java.core.serde.TypeRegistry;
import software.amazon.smithy.java.io.uri.URIBuilder;
import software.amazon.smithy.java.server.core.InMemoryRequest;
import software.amazon.smithy.java.server.core.InMemoryResponse;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.protocol.traits.InMemoryCborTrait;
import software.amazon.smithy.protocol.traits.InMemoryJavaTrait;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class InMemoryJavaProtocol extends InMemoryClientProtocol<InMemoryRequest, InMemoryResponse> {
    private static final List<String> SMITHY_PROTOCOL = List.of("in-memory-v1-java");

    private final ShapeId service;

    public InMemoryJavaProtocol(ShapeId service) {
        super(InMemoryCborTrait.ID.toString());
        this.service = service;
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
    public <I extends SerializableStruct, O extends SerializableStruct> InMemoryRequest createRequest(
            ApiOperation<I, O> operation,
            I input,
            Context context,
            URI endpoint
    ) {
        var target = "/service/" + service.getName() + "/operation/" + operation.schema().id().getName();
        var uri = endpoint.resolve(target);

        context.put(InMemoryRequest.SMITHY_PROTOCOL_KEY, InMemoryJavaTrait.ID);
        var result = new InMemoryRequest(uri, null);
        // TODO: wrong type registry, needs to be the in-memory service type registry
        var serializer = new ConsumerSerializer(operation.typeRegistry(), (schema, value) -> {
            result.setSerializedValue(value);
        });
        input.serialize(serializer);

        return result;
    }

    @Override
    public InMemoryRequest setServiceEndpoint(InMemoryRequest request, Endpoint endpoint) {
        var uri = endpoint.uri();
        URIBuilder builder = URIBuilder.of(request.getUri());

        if (uri.getScheme() != null) {
            builder.scheme(uri.getScheme());
        }

        if (uri.getHost() != null) {
            builder.host(uri.getHost());
        }

        if (uri.getPort() > -1) {
            builder.port(uri.getPort());
        }

        // If a path is set on the service endpoint, concatenate it with the path of the request.
        if (uri.getRawPath() != null && !uri.getRawPath().isEmpty()) {
            builder.path(uri.getRawPath());
            builder.concatPath(request.getUri().getPath());
        }

        request.setUri(builder.build());

        return request;
    }

    @Override
    public <I extends SerializableStruct, O extends SerializableStruct> CompletableFuture<O> deserializeResponse(
            ApiOperation<I, O> operation,
            Context context,
            TypeRegistry typeRegistry,
            InMemoryRequest request,
            InMemoryResponse response
    ) {
        var result = new CompletableFuture();
        var serializer = new ConsumerSerializer(operation.typeRegistry(), (schema, value) -> {
            result.complete(value);
        });
        // TODO: Need Deserializer equivalent of ConsumerSerializer

        return result;
    }

    public static final class Factory implements ClientProtocolFactory<InMemoryCborTrait> {
        @Override
        public ShapeId id() {
            return InMemoryCborTrait.ID;
        }

        @Override
        public ClientProtocol<?, ?> createProtocol(ProtocolSettings settings, InMemoryCborTrait trait) {
            return new InMemoryJavaProtocol(
                    Objects.requireNonNull(
                            settings.service(),
                            "service is a required protocol setting"
                    )
            );
        }
    }
}
