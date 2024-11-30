/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.server.core;

import java.net.URI;
import software.amazon.smithy.java.http.api.HttpHeaders;
import software.amazon.smithy.java.io.datastream.DataStream;

// TODO see if we can reuse SmithyHttpRequest in here.
public final class HttpRequest extends RequestImpl {

    private final HttpHeaders headers;
    private final URI uri;
    private final String method;
    private DataStream dataStream;

    public HttpRequest(HttpHeaders headers, URI uri, String method) {
        this.headers = headers;
        this.uri = uri;
        this.method = method;
    }

    public HttpHeaders headers() {
        return headers;
    }

    public URI uri() {
        return uri;
    }

    public String method() {
        return method;
    }

    public DataStream getDataStream() {
        return dataStream;
    }

    public void setDataStream(DataStream dataStream) {
        this.dataStream = dataStream;
    }
}
