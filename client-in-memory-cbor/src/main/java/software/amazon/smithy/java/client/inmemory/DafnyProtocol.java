package software.amazon.smithy.java.client.inmemory;

import software.amazon.smithy.java.client.core.ClientProtocol;
import software.amazon.smithy.java.client.core.endpoint.Endpoint;
import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.core.schema.ApiOperation;
import software.amazon.smithy.java.core.schema.SerializableStruct;
import software.amazon.smithy.java.core.serde.TypeRegistry;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class DafnyProtocol implements ClientProtocol<Object, Object> {
    @Override
    public String id() {
        return "";
    }

    @Override
    public Class<Object> requestClass() {
        return Object.class;
    }

    @Override
    public Class<Object> responseClass() {
        return Object.class;
    }

    @Override
    public <I extends SerializableStruct, O extends SerializableStruct> Object createRequest(ApiOperation<I, O> operation, I input, Context context, URI endpoint) {
        return null;
    }

    @Override
    public Object setServiceEndpoint(Object request, Endpoint endpoint) {
        return null;
    }

    @Override
    public <I extends SerializableStruct, O extends SerializableStruct> CompletableFuture<O> deserializeResponse(ApiOperation<I, O> operation, Context context, TypeRegistry typeRegistry, Object request, Object response) {
        return null;
    }
}
