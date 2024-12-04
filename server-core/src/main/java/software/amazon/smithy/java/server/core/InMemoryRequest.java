package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.model.shapes.ShapeId;

import java.net.URI;

public final class InMemoryRequest extends RequestImpl {

    // TODO: Move this somewhere more central
    public static final Context.Key<ShapeId> SMITHY_PROTOCOL_KEY = Context
            .key("In-Memory Smithy Protocol");

    private URI uri;
    // TODO: Introduce type parameters, and/or dynamic type saftey checks ala client protocols
    private Object serializedValue;

    public InMemoryRequest(URI uri, Object serializedValue) {
        this.uri = uri;
        this.serializedValue = serializedValue;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Object getSerializedValue() {
        return serializedValue;
    }

    public void setSerializedValue(Object serializedValue) {
        this.serializedValue = serializedValue;
    }
}
