package software.amazon.smithy.java.server.example;

import software.amazon.smithy.java.client.core.endpoint.EndpointResolver;
import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.core.endpoint.Endpoint;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.java.example.model.ResolvedEndpoint;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

// TODO: Should be in a core smithy-java module instead
public class ResolvedEndpointResolver {

    public static Endpoint endpoint(ResolvedEndpoint resolvedEndpoint) {
        var builder = Endpoint.builder()
                .uri(resolvedEndpoint.url())
                .channelUri(resolvedEndpoint.channelUrl());
        for (Map.Entry<String, Document> entry : resolvedEndpoint.properties().entrySet()) {
            builder.putProperty(Context.key(entry.getKey()), entry.getValue());
        }
        return builder.build();
    }

    public static EndpointResolver staticResolvedEndpoint(ResolvedEndpoint resolvedEndpoint) {
        return params -> CompletableFuture.completedFuture(endpoint(resolvedEndpoint));
    };
}
