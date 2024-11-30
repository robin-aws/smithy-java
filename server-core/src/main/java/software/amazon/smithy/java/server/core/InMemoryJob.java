package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.core.schema.SerializableStruct;
import software.amazon.smithy.java.server.Operation;

public final class InMemoryJob extends DefaultJob {

    private final InMemoryDataStreamRequest request;
    private final InMemoryDataStreamResponse response;

    public InMemoryJob(Operation<? extends SerializableStruct, ? extends SerializableStruct> operation,
                          ServerProtocol protocol,
                          InMemoryDataStreamRequest request,
                          InMemoryDataStreamResponse response) {
        super(operation, protocol);
        this.request = request;
        this.response = response;
    }

    @Override
    public Request request() {
        return request;
    }

    @Override
    public Response response() {
        return response;
    }
}
