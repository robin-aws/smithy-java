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
 *
 * TODO: This might be useless since there isn't actually aren't common request/response types.
 */
public abstract class InMemoryClientProtocol<RequestT, ResponseT> implements ClientProtocol<RequestT, ResponseT> {

    private final String id;

    public InMemoryClientProtocol(String id) {
        this.id = id;
    }

    @Override
    public final String id() {
        return id;
    }
}
