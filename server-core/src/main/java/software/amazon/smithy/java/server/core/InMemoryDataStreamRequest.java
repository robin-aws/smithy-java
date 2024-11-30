package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.io.datastream.DataStream;

public final class InMemoryDataStreamRequest extends RequestImpl {

    private DataStream dataStream;

    public InMemoryDataStreamRequest(DataStream dataStream) {
        this.dataStream = dataStream;
    }

    public DataStream getDataStream() {
        return dataStream;
    }

    public void setDataStream(DataStream dataStream) {
        this.dataStream = dataStream;
    }
}
