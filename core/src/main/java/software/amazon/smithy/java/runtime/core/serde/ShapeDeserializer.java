/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.runtime.core.serde;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import software.amazon.smithy.java.runtime.core.schema.SdkSchema;
import software.amazon.smithy.java.runtime.core.serde.document.Document;

/**
 * Deserializes a shape by emitted the Smithy data model from the shape, aided by schemas.
 */
public interface ShapeDeserializer extends AutoCloseable {

    @Override
    default void close() {}

    /**
     * Attempt to read a boolean value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    boolean readBoolean(SdkSchema schema);

    /**
     * Attempt to read a blob value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    byte[] readBlob(SdkSchema schema);

    /**
     * Attempt to read a byte value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    byte readByte(SdkSchema schema);

    /**
     * Attempt to read a short value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    short readShort(SdkSchema schema);

    /**
     * Attempt to read an integer or intEnum value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    int readInteger(SdkSchema schema);

    /**
     * Attempt to read a long value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    long readLong(SdkSchema schema);

    /**
     * Attempt to read a float value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    float readFloat(SdkSchema schema);

    /**
     * Attempt to read a double value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    double readDouble(SdkSchema schema);

    /**
     * Attempt to read a bigInteger value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    BigInteger readBigInteger(SdkSchema schema);

    /**
     * Attempt to read a bigDecimal value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    BigDecimal readBigDecimal(SdkSchema schema);

    /**
     * Attempt to read a string or enum value.
     *
     * @param schema Schema of the shape.
     * @return the read value.
     */
    String readString(SdkSchema schema);

    /**
     * Attempt to read a document value.
     *
     * @return the read value.
     */
    Document readDocument();

    /**
     * Attempt to read a timestamp value.
     *
     * @return the read value.
     */
    Instant readTimestamp(SdkSchema schema);

    /**
     * Attempt to read a structure or union value.
     *
     * @param schema   Schema of the shape.
     * @param state    State to pass to the consumer.
     * @param consumer Consumer that receives the state, member schema, and deserializer.
     */
    <T> void readStruct(SdkSchema schema, T state, StructMemberConsumer<T> consumer);

    /**
     * Attempt to read a list value.
     *
     * @param schema   Schema of the shape.
     * @param state    State to pass to the consumer.
     * @param consumer Consumer that receives the state and deserializer.
     */
    <T> void readList(SdkSchema schema, T state, ListMemberConsumer<T> consumer);

    /**
     * Attempt to read a map value.
     *
     * @param schema   Schema of the shape.
     * @param state    State to pass to the consumer.
     * @param consumer Consumer that receives the state, map key, and deserializer.
     */
    <T> void readStringMap(SdkSchema schema, T state, MapMemberConsumer<String, T> consumer);

    /**
     * Consumer of a structure member.
     *
     * @param <T> Passed in state value to avoid capturing external state.
     */
    @FunctionalInterface
    interface StructMemberConsumer<T> {
        void accept(T state, SdkSchema memberSchema, ShapeDeserializer memberDeserializer);
    }

    /**
     * Consumer of list member.
     *
     * @param <T> Passed in state value to avoid capturing external state.
     */
    @FunctionalInterface
    interface ListMemberConsumer<T> {
        void accept(T state, ShapeDeserializer memberDeserializer);
    }

    /**
     * Consumer of map member.
     *
     * @param <T> Passed in state value to avoid capturing external state.
     */
    @FunctionalInterface
    interface MapMemberConsumer<K, T> {
        void accept(T state, K key, ShapeDeserializer memberDeserializer);
    }
}
