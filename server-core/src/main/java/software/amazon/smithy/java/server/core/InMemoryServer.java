package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.server.Server;

import java.util.concurrent.CompletableFuture;

public class InMemoryServer implements Server {

    // TODO: Refine this. Some kind of safe static state is ultimately necessary.
    // More generally there could be multiple servers listening to
    // different endpoints, but that introduces overhead without any obvious benefit.
    // At a minimum there should be better matching than just iterating over lists.
    public volatile static InMemoryServer SERVER = null;

    private final Orchestrator orchestrator;
    private final ProtocolResolver resolver;

    public InMemoryServer(InMemoryServerBuilder builder) {
        resolver = new ProtocolResolver(builder.serviceMatcher);

        var handlers = new HandlerAssembler().assembleHandlers(builder.serviceMatcher.getAllServices());
        orchestrator = new OrchestratorGroup(
                builder.numberOfWorkers,
                () -> new ErrorHandlingOrchestrator(new SingleThreadOrchestrator(handlers)),
                OrchestratorGroup.Strategy.roundRobin()
        );
    }

    // TODO: generalize beyond just data stream payloads
    public CompletableFuture<InMemoryResponse> handle(Context context, InMemoryRequest request) {
        var resolutionResult = resolver.resolve(
                new ServiceProtocolResolutionRequest(request.getUri(), null, context, null)
        );
        var response = new InMemoryResponse();
        var job = new InMemoryJob(resolutionResult.operation(), resolutionResult.protocol(), request, response);
        return orchestrator.enqueue(job)
                .thenCompose(r -> CompletableFuture.completedFuture(response));
    }

    @Override
    public void start() {
        if (SERVER != null) {
            throw new IllegalStateException("Server is already started");
        }
        SERVER = this;
    }

    @Override
    public CompletableFuture<Void> shutdown() {
        // TODO: flush jobs
        SERVER = null;
        return CompletableFuture.completedFuture(null);
    }
}
