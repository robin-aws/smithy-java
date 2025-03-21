/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.client.core;

import software.amazon.smithy.model.shapes.ShapeId;

/**
 * Settings used to instantiate a {@link ClientProtocol} implementation.
 */
public final class ProtocolSettings {
    private final ShapeId service;

    private ProtocolSettings(Builder builder) {
        this.service = builder.service;
    }

    public ShapeId service() {
        return service;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ShapeId service;

        private Builder() {}

        public Builder service(ShapeId service) {
            this.service = service;
            return this;
        }

        public ProtocolSettings build() {
            return new ProtocolSettings(this);
        }
    }
}
