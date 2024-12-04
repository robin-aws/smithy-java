package software.amazon.smithy.java.core.serde;

import software.amazon.smithy.java.core.schema.Schema;
import software.amazon.smithy.java.core.schema.SerializableStruct;
import software.amazon.smithy.java.core.serde.document.Document;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

// TODO: This seemed super useful until I realized TypeRegistry's were mostly only
// used for errors.
public class ConsumerSerializer implements ShapeSerializer {

    private final TypeRegistry typeRegistry;
    private final BiConsumer<Schema, Object> valueConsumer;

    public ConsumerSerializer(TypeRegistry typeRegistry, BiConsumer<Schema, Object> valueConsumer) {
        this.valueConsumer = valueConsumer;
        this.typeRegistry = typeRegistry;
    }

    @Override
    public void writeStruct(Schema schema, SerializableStruct struct) {
        var builder = typeRegistry.createBuilder(schema.id());
        var serializer = new ConsumerSerializer(typeRegistry, builder::setMemberValue);
        struct.serialize(serializer);
        valueConsumer.accept(schema, builder.build());
    }

    @Override
    public <T> void writeList(Schema schema, T listState, int size, BiConsumer<T, ShapeSerializer> consumer) {
        var value = new ArrayList<>(size);
        var serializer = new ConsumerSerializer(typeRegistry, (member, o) -> value.add(o));
        consumer.accept(listState, serializer);
        valueConsumer.accept(schema, value);
    }

    @Override
    public <T> void writeMap(Schema schema, T mapState, int size, BiConsumer<T, MapSerializer> consumer) {
        var value = new HashMap<>(size);
        MapSerializer serializer;
        serializer = new ConsumerMapSerializer(typeRegistry, value::put);
        consumer.accept(mapState, serializer);
        valueConsumer.accept(schema, value);
    }

    private record ConsumerMapSerializer(TypeRegistry typeRegistry,
                                         BiConsumer<String, Object> entryConsumer) implements MapSerializer {

        @Override
        public <T> void writeEntry(Schema keySchema, String key, T state, BiConsumer<T, ShapeSerializer> valueSerializer) {
            var s = new ConsumerSerializer(typeRegistry, (valueSchema, value) -> entryConsumer.accept(key, value));
            valueSerializer.accept(state, s);
        }
    }

    @Override
    public void writeBoolean(Schema schema, boolean value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeByte(Schema schema, byte value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeShort(Schema schema, short value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeInteger(Schema schema, int value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeLong(Schema schema, long value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeFloat(Schema schema, float value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeDouble(Schema schema, double value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeBigInteger(Schema schema, BigInteger value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeBigDecimal(Schema schema, BigDecimal value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeString(Schema schema, String value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeBlob(Schema schema, ByteBuffer value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeTimestamp(Schema schema, Instant value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeDocument(Schema schema, Document value) {
        valueConsumer.accept(schema, value);
    }

    @Override
    public void writeNull(Schema schema) {
        valueConsumer.accept(schema, null);
    }
}
