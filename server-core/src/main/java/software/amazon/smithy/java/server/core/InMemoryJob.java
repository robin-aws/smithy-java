package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.core.schema.SerializableStruct;
import software.amazon.smithy.java.server.Operation;

public final class InMemoryJob extends DefaultJob {

    private final InMemoryServerRequest request;
    private final InMemoryServerResponse response;

    public InMemoryJob(Operation<? extends SerializableStruct, ? extends SerializableStruct> operation,
                          ServerProtocol protocol,
                          InMemoryServerRequest request,
                          InMemoryServerResponse response) {
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
