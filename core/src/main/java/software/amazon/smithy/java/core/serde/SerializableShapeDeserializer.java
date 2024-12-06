package software.amazon.smithy.java.core.serde;

import software.amazon.smithy.java.core.schema.Schema;
import software.amazon.smithy.java.core.schema.SerializableStruct;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.model.shapes.ShapeType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class SerializableShapeDeserializer implements ShapeDeserializer {

    private final Object value;

    public SerializableShapeDeserializer(Object value) {
        this.value = value;
    }

    @Override
    public boolean readBoolean(Schema schema) {
        return (Boolean)value;
    }

    @Override
    public ByteBuffer readBlob(Schema schema) {
        return (ByteBuffer)value;
    }

    @Override
    public byte readByte(Schema schema) {
        return (Byte)value;
    }

    @Override
    public short readShort(Schema schema) {
        return (Short)value;
    }

    @Override
    public int readInteger(Schema schema) {
        return (Integer)value;
    }

    @Override
    public long readLong(Schema schema) {
        return (Long)value;
    }

    @Override
    public float readFloat(Schema schema) {
        return (Float)value;
    }

    @Override
    public double readDouble(Schema schema) {
        return (Double)value;
    }

    @Override
    public BigInteger readBigInteger(Schema schema) {
        return (BigInteger)value;
    }

    @Override
    public BigDecimal readBigDecimal(Schema schema) {
        return (BigDecimal)value;
    }

    @Override
    public String readString(Schema schema) {
        if (schema.type().isShapeType(ShapeType.ENUM)) {
            // TODO: Is there a better way? There isn't a common supertype
            // like there is with SerializableStruct.
            return value.toString();
        } else {
            return (String) value;
        }
    }

    @Override
    public Document readDocument() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Instant readTimestamp(Schema schema) {
        return (Instant)value;
    }


    @Override
    public <T> void readStruct(Schema schema, T state, StructMemberConsumer<T> consumer) {
        var struct = (SerializableStruct)value;
        for (Schema member: schema.members()) {
            consumer.accept(state, member, new SerializableShapeDeserializer(struct.getMemberValue(member)));
        }
    }

    @Override
    public <T> void readList(Schema schema, T state, ListMemberConsumer<T> consumer) {
        List<?> list = (List<?>)value;
        for (Object element : list) {
            consumer.accept(state, new SerializableShapeDeserializer(element));
        }
    }

    @Override
    public <T> void readStringMap(Schema schema, T state, MapMemberConsumer<String, T> consumer) {
        Map<String, ?> map = (Map<String, ?>)value;
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            consumer.accept(state, entry.getKey(), new SerializableShapeDeserializer(entry.getValue()));
        }
    }

    @Override
    public boolean isNull() {
        return false;
    }
}
