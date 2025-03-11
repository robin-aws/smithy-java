package software.amazon.smithy.java.server.example;

import software.amazon.smithy.java.example.callbacks.model.NotifyCompletedInput;
import software.amazon.smithy.java.example.callbacks.model.NotifyCompletedOutput;
import software.amazon.smithy.java.example.callbacks.service.CoffeeShopCallbacks;
import software.amazon.smithy.java.example.model.ResolvedEndpoint;
import software.amazon.smithy.java.server.RequestContext;
import software.amazon.smithy.java.server.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class CoffeeCallbacks {

    private static Server SERVER;
    private static ResolvedEndpoint ENDPOINT;

    public static synchronized ResolvedEndpoint getEndpoint() {
        if (SERVER == null) {
            ENDPOINT = ResolvedEndpoint.builder()
                    .url("http://localhost/")
                        .channelUrl("unix:%s/.aws/localservices/coffeeshopcallbacks/%s".formatted(
                        System.getProperty("user.home"), ProcessHandle.current().pid()))
                    .build();
            var endpoint = ResolvedEndpointResolver.endpoint(ENDPOINT);
            SERVER = Server.builder("smithy-java-netty-server")
                    .endpoints(endpoint)
                    .addService(
                            CoffeeShopCallbacks.builder()
                                    .addNotifyCompletedOperation(CoffeeCallbacks::notifyCompleted)
                                    .build()
                    )
                    .build();
            SERVER.start();
        }
        return ENDPOINT;
    }

    interface CompletedCallback {
        CompletableFuture<NotifyCompletedOutput> notifyCompleted(NotifyCompletedInput input, RequestContext context);
    }

    private static final Map<String, CompletedCallback> callbacksById = new HashMap<>();

    public static CompletableFuture<NotifyCompletedOutput> notifyCompleted(NotifyCompletedInput input, RequestContext context) {
        // Delete the callback at the same time,
        // since these are single-use resources.
        // In many APIs the callback would be static,
        // or only deleted by an explicit operation
        // ("delete" in the resource's lifecycle)
        var callback = callbacksById.remove(input.callbackId());
        return callback.notifyCompleted(input, context);
    }

    public static String registerCallback(CompletedCallback callback) {
        var id = UUID.randomUUID().toString();
        callbacksById.put(id, callback);
        return id;
    }
}
