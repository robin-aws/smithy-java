package software.amazon.smithy.java.server.example;

import software.amazon.smithy.java.core.endpoint.Endpoint;

/**
 * Things that should be attached to a proper @aws.api#localService trait.
 * Having all Smithy code generation target languages able to calculate consistent endpoints
 * allows them to communicate with each other with no other FFI or coordination.
 */
public class LocalService {

    /**
     * Convention for the default endpoint for aws.api#localService deployed in-memory.
     * Includes the process ID to ensure no cross-talk.
     */
    public static Endpoint defaultInMemoryEndpoint(String serviceName) {
        return Endpoint.builder()
            .uri("http://localhost/")
            .channelUri("unix:%s/.aws/localservices/%s/%s".formatted(
                    System.getProperty("user.home"), serviceName, ProcessHandle.current().pid()))
            .build();
    }

    /**
     * Convention for the default endpoint for aws.api#localService deployed
     * as a local daemon.
     */
    public static Endpoint defaultDaemonEndpoint(String serviceName) {
        return Endpoint.builder()
            .uri("http://localhost/")
            .channelUri("unix:%s/.aws/localservices/%s/daemon".formatted(
                    System.getProperty("user.home"), serviceName))
            .build();
    }
}
