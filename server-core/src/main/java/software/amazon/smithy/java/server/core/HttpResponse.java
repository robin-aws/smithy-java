/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.http.api.ModifiableHttpHeaders;
import software.amazon.smithy.java.io.datastream.DataStream;

public final class HttpResponse extends ResponseImpl {

    private int statusCode;
    private DataStream dataStream;

    public HttpResponse(ModifiableHttpHeaders headers) {
        this.headers = headers;
    }

    private final ModifiableHttpHeaders headers;

    public ModifiableHttpHeaders headers() {
        return headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setSerializedValue(DataStream serializedValue) {
        this.dataStream = serializedValue;
    }

    public DataStream getSerializedValue() {
        return dataStream;
    }
}
