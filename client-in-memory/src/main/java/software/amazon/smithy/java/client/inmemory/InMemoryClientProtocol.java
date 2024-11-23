/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.client.inmemory;

import software.amazon.smithy.java.client.core.ClientProtocol;
import software.amazon.smithy.java.client.core.endpoint.Endpoint;
import software.amazon.smithy.java.inmemory.api.InMemoryRequest;
import software.amazon.smithy.java.inmemory.api.InMemoryResponse;

/**
 * An abstract class for implementing in-memory protocols.
 */
public abstract class InMemoryClientProtocol implements ClientProtocol<InMemoryRequest, InMemoryResponse> {

    private final String id;

    public InMemoryClientProtocol(String id) {
        this.id = id;
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final Class<InMemoryRequest> requestClass() {
        return InMemoryRequest.class;
    }

    @Override
    public final Class<InMemoryResponse> responseClass() {
        return InMemoryResponse.class;
    }

    @Override
    public InMemoryRequest setServiceEndpoint(InMemoryRequest request, Endpoint endpoint) {
        // TODO: probably a map lookup of some kind to find the concrete implementation
        return request;
    }
}
