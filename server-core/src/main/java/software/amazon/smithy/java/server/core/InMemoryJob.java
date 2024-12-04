package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.core.schema.SerializableStruct;
import software.amazon.smithy.java.server.Operation;

public final class InMemoryJob extends DefaultJob {

    private final InMemoryRequest request;
    private final InMemoryResponse response;

    public InMemoryJob(Operation<? extends SerializableStruct, ? extends SerializableStruct> operation,
                          ServerProtocol protocol,
                          InMemoryRequest request,
                          InMemoryResponse response) {
        super(operation, protocol);
        this.request = request;
        this.response = response;
    }

    @Override
    public InMemoryRequest request() {
        return request;
    }

    @Override
    public InMemoryResponse response() {
        return response;
    }
}
