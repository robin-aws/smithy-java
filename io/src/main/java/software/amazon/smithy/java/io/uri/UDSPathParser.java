package software.amazon.smithy.java.io.uri;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

/**
 * A WIP simple implementation of embedding a symbolic Unix Domain Sockets path
 * in the host of a URI.
 *
 * See https://github.com/whatwg/url/issues/577 for much more detail
 * on the options here. I've gone with the only feasible option
 * that the java.net.URI parser will accept
 * (it doesn't support the RFC 3986 [v###.<etc>] syntax yet:
 * https://bugs.openjdk.org/browse/JDK-8019345)
 */
public class UDSPathParser {

    // TODO: Assuming AWS-specific conventions similar to @aws.api#service.
    private static final String UDS_LOCALHOST = ".aws.uds.localhost";

    // The root path bound to UDS_LOCALHOST.
    //
    // TODO: Platform-specific, no windows support yet,
    // but should be %USERPROFILE%\.aws\localservices
    private static final Path UDS_ROOT_PATH = Path.of("/Users/salkeldr/.aws/localservices");

    public static boolean isUDS(URI uri) {
        return uri.getHost().endsWith(UDS_LOCALHOST);
    }

    // TODO: This should be split into parsing and resolution phases.
    public static Path parseAndResolveUDSPath(URI uri) {
        if (!isUDS(uri)) {
            throw new IllegalArgumentException("Invalid uri: " + uri);
        }

        final var host = uri.getHost();
        final var symbolicPath = host.substring(0, host.length() - UDS_LOCALHOST.length());
        final var segments = symbolicPath.split("\\.");
        Collections.reverse(Arrays.asList(segments));
        // TODO: Some degree of escaping is probably a good idea. Is percent encoding enough?
        var path = UDS_ROOT_PATH;
        for (String segment : segments) {
            path = path.resolve(segment);
        }

        return path;
    }
}
