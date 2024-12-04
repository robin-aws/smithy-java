/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.core.schema.SerializableStruct;

public abstract sealed class RequestImpl implements Request permits HttpRequest, InMemoryRequest {

    private final Context context = Context.create();
    private SerializableStruct deserializedValue;

    @Override
    public final Context context() {
        return context;
    }

    @Override
    public <T extends SerializableStruct> T getDeserializedValue() {
        return (T) deserializedValue;
    }

    @Override
    public void setDeserializedValue(SerializableStruct serializableStruct) {
        this.deserializedValue = serializableStruct;
    }
}
