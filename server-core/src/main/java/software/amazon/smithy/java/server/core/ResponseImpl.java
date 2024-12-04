/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.server.core;

import software.amazon.smithy.java.context.Context;
import software.amazon.smithy.java.core.schema.SerializableStruct;

public abstract sealed class ResponseImpl implements Response permits HttpResponse, InMemoryResponse {

    private final Context context = Context.create();
    private SerializableStruct value;

    @Override
    public final Context context() {
        return context;
    }

    @Override
    public void setValue(SerializableStruct value) {
        this.value = value;
    }

    @Override
    public <T extends SerializableStruct> T getValue() {
        return (T) value;
    }
}
