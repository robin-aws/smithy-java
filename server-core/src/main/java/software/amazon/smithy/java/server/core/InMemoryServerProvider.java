package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.server.ServerBuilder;
import software.amazon.smithy.java.server.ServerProvider;

public class InMemoryServerProvider implements ServerProvider {
    @Override
    public String name() {
        return "smithy-java-in-memory-server";
    }

    @Override
    public ServerBuilder<?> serverBuilder() {
        return new InMemoryServerBuilder();
    }

    @Override
    public int priority() {
        return 1;
    }
}
