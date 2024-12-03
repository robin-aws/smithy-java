package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.server.Route;
import software.amazon.smithy.java.server.Server;
import software.amazon.smithy.java.server.ServerBuilder;

import java.net.URI;
import java.util.List;

public class InMemoryServerBuilder extends ServerBuilder<InMemoryServerBuilder> {

    ServiceMatcher serviceMatcher;
    List<URI> endpoints;
    int numberOfWorkers = Runtime.getRuntime().availableProcessors() * 2;

    @Override
    public InMemoryServerBuilder endpoints(URI... endpoints) {
        this.endpoints = List.of(endpoints);
        return self();
    }

    @Override
    public InMemoryServerBuilder numberOfWorkers(int numberOfWorkers) {
        this.numberOfWorkers = numberOfWorkers;
        return self();
    }

    @Override
    protected InMemoryServerBuilder setServerRoutes(List<Route> routes) {
        this.serviceMatcher = new ServiceMatcher(routes);
        return self();
    }

    @Override
    protected InMemoryServer buildServer() {
        validate();
        return new InMemoryServer(this);
    }

    private void validate() {
        if (numberOfWorkers <= 0) {
            throw new IllegalArgumentException("Number of workers must be greater than zero");
        }
    }
}
