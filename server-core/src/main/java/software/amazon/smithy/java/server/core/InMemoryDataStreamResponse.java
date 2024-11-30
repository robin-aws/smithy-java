package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.io.datastream.DataStream;

public final class InMemoryDataStreamResponse extends ResponseImpl {
    private DataStream dataStream;

    public InMemoryDataStreamResponse() {
    }

    @Override
    public DataStream getSerializedValue() {
        return dataStream;
    }

    @Override
    public void setSerializedValue(DataStream dataStream) {
        this.dataStream = dataStream;
    }
}
