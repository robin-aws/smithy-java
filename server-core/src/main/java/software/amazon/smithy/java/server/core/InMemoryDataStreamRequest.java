package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.io.datastream.DataStream;
import software.amazon.smithy.model.shapes.ShapeId;

import java.net.URI;

public final class InMemoryDataStreamRequest extends RequestImpl {

    // TODO: Move this somewhere more central
    public static final Context.Key<ShapeId> SMITHY_PROTOCOL_KEY = Context
            .key("In-Memory Smithy Protocol");

    private URI uri;
    private DataStream dataStream;

    public InMemoryDataStreamRequest(URI uri, DataStream dataStream) {
        this.uri = uri;
        this.dataStream = dataStream;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public DataStream getDataStream() {
        return dataStream;
    }

    public void setDataStream(DataStream dataStream) {
        this.dataStream = dataStream;
    }
}
