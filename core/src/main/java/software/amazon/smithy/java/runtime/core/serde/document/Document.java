/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.runtime.core.serde.document;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import software.amazon.smithy.java.runtime.core.schema.PreludeSchemas;
import software.amazon.smithy.java.runtime.core.schema.Schema;
import software.amazon.smithy.java.runtime.core.schema.SerializableShape;
import software.amazon.smithy.java.runtime.core.schema.ShapeBuilder;
import software.amazon.smithy.java.runtime.core.serde.SerializationException;
import software.amazon.smithy.java.runtime.core.serde.ShapeSerializer;
import software.amazon.smithy.model.shapes.ShapeType;

/**
 * A Smithy document type, representing untyped data from the Smithy data model.
 *
 * <h3>Documents and the Smithy data model</h3>
 *
 * <p>The Smithy data model consists of:
 *
 * <ul>
 *     <li>Numbers: byte, short, integer, long, float, double, bigInteger, bigDecimal. IntEnum shapes are
 *         represented as integers in the Smithy data model.</li>
 *     <li>boolean</li>
 *     <li>blob</li>
 *     <li>string: enum shapes are represented as strings in the Smithy data model</li>
 *     <li>timestamp: Represented as an {@link Instant}</li>
 *     <li>list: list of Documents</li>
 *     <li>map: map of int|long|string keys to Document values</li>
 *     <li>struct: structure or union</li>
 * </ul>
 *
 * <h3>Serializing documents and their contents</h3>
 *
 * <p>A document type implements {@link SerializableShape} and implementations must always call
 * {@link ShapeSerializer#writeDocument}. Shape serializers can access the contents of the document using
 * {@link #serializeContents}, which emits the Smithy data model based on the contents of the document.
 * The first shape written with {@link #serializeContents} must not be a document because doing so would cause
 * infinite recursion in serializers.
 *
 * <h3>Protocol smoothing, string and blob interop</h3>
 *
 * <p>Document types are a protocol-agnostic view of untyped data. Protocol codecs should attempt to smooth over
 * protocol incompatibilities with the Smithy data model. If a protocol serializes a blob as a base64 encoded string,
 * then calling {@link #asBlob()} should automatically base64 decode the value and return the underlying bytes.
 *
 * <h3>Typed documents</h3>
 *
 * <p>Document types can be combined with a typed schema using {@link #createTyped(SerializableShape)}. This kind of
 * document type allows (but does not require) codecs to serialize or deserialize the document exactly as if the shape
 * itself was serialized or deserialized directly.
 */
public interface Document extends SerializableShape {
    /**
     * Get the Smithy data model type for the underlying contents of the document.
     *
     * <p>The type can be used to know the appropriate "as" method to call to get the underlying data of the document.
     *
     * <p>The type returned from this method will differ from the type and schema emitted from
     * {@link #serialize(ShapeSerializer)}, which always writes the document as {@link ShapeSerializer#writeDocument}.
     * However, the type returned from this method should correspond to the type emitted from
     * {@link #serializeContents(ShapeSerializer)}.
     *
     * <ul>
     *     <li>enum shapes: Enum shapes are treated as a {@link ShapeType#STRING}, and variants can be found in
     *     the corresponding schema emitted from {@link #serializeContents(ShapeSerializer)}.</li>
     *     <li>intEnum shapes: Enum shapes are treated as an {@link ShapeType#INTEGER}, and variants can be found in
     *     the corresponding schema emitted from {@link #serializeContents(ShapeSerializer)}.</li>
     * </ul>
     *
     * @return the Smithy data model type.
     */
    ShapeType type();

    /**
     * Serializes the Document as a document value in the Smithy data model.
     *
     * <p>All implementations of a document type are expected to follow the same behavior as this method when writing
     * to a {@link ShapeSerializer}; the document is always written with {@link ShapeSerializer#writeDocument(Schema, Document)}
     * and receivers are free to query the underlying contents of the document using
     * {@link #serializeContents(ShapeSerializer)}.
     *
     * @param serializer Where to send the document to {@link ShapeSerializer#writeDocument(Schema, Document)}.
     */
    @Override
    default void serialize(ShapeSerializer serializer) {
        serializer.writeDocument(PreludeSchemas.DOCUMENT, this);
    }

    /**
     * Serialize the contents of the document using the Smithy data model and an appropriate {@link javax.xml.validation.Schema}.
     *
     * <p>While {@link #serialize(ShapeSerializer)} always emits document values as
     * {@link ShapeSerializer#writeDocument(Schema, Document)}, this method emits the contents of the document itself.
     * {@code ShapeSerializer} implementations that receive a {@link Document} via {@code writeDocument} can get the
     * inner contents of the document using this method.
     *
     * <p>When implementing this method, each call to the serializer must provide the most appropriate schema
     * possible to capture the underlying document value. Documents that are not typed to a specific shape should use
     * schemas from {@link PreludeSchemas}. For example, a string document should use {@link PreludeSchemas#STRING},
     * and an integer document should use {@link PreludeSchemas#INTEGER}.
     *
     * <p>Structure, list, and map documents that are not typed to a specific shape should use
     * {@link PreludeSchemas#DOCUMENT}. Members emitted for these shapes must include an appropriate member name
     * and target shape. For example, to represent the value of a map entry, the value must be emitted as a member
     * schema (even a synthetic member) with a member name of "value" and the document value target
     * (e.g., {@code smithy.api#Document$value}). In doing so, receivers of the document's data model do not need to
     * implement special-cased logic to account for synthetic document type members vs. actual modeled members
     *
     * <p>Implementations must not write the contents of the document as {@link ShapeSerializer#writeDocument(Schema, Document)}
     * because that could result in infinite recursion for serializers that want access to the contents of a document.
     *
     * @param serializer Serializer to write the underlying data of the document to.
     */
    void serializeContents(ShapeSerializer serializer);

    /**
     * Get the boolean value of the Document if it is a boolean.
     *
     * @return the boolean value.
     * @throws SerializationException if the Document is not a boolean.
     */
    default boolean asBoolean() {
        throw new SerializationException("Expected a boolean document, but found " + type());
    }

    /**
     * Get the byte value of the Document if it is a byte.
     *
     * <p>If the value is a number of a different type, the value is cast, which can result in a loss of precision.
     *
     * @return the byte value.
     * @throws SerializationException if the Document is not a number.
     * @throws ArithmeticException if the value is out of range for this type.
     */
    default byte asByte() {
        throw new SerializationException("Expected a byte document, but found " + type());
    }

    /**
     * Get the short value of the Document if it is a short.
     *
     * <p>If the value is a number of a different type, the value is cast, which can result in a loss of precision.
     *
     * @return the short value.
     * @throws SerializationException if the Document is not a number.
     * @throws ArithmeticException if the value is out of range for this type.
     */
    default short asShort() {
        throw new SerializationException("Expected a short document, but found " + type());
    }

    /**
     * Get the integer value of the Document if it is an integer.
     *
     * <p>If the value is a number of a different type, the value is cast, which can result in a loss of precision.
     *
     * @return the integer value.
     * @throws SerializationException if the Document is not a number.
     * @throws ArithmeticException if the value is out of range for this type.
     */
    default int asInteger() {
        throw new SerializationException("Expected an integer document, but found " + type());
    }

    /**
     * Get the long value of the Document if it is a long.
     *
     * <p>If the value is a number of a different type, the value is cast, which can result in a loss of precision.
     *
     * @return the long value.
     * @throws SerializationException if the Document is not a number.
     * @throws ArithmeticException if the value is out of range for this type.
     */
    default long asLong() {
        throw new SerializationException("Expected a long document, but found " + type());
    }

    /**
     * Get the float value of the Document if it is a float.
     *
     * <p>If the value is a number of a different type, the value is cast, which can result in a loss of precision.
     *
     * @return the float value.
     * @throws SerializationException if the Document is not a number.
     * @throws ArithmeticException if the value is out of range for this type.
     */
    default float asFloat() {
        throw new SerializationException("Expected a float document, but found " + type());
    }

    /**
     * Get the double value of the Document if it is a double.
     *
     * <p>If the value is a number of a different type, the value is cast, which can result in a loss of precision.
     *
     * @return the double value.
     * @throws SerializationException if the Document is not a number.
     * @throws ArithmeticException if the value is out of range for this type.
     */
    default double asDouble() {
        throw new SerializationException("Expected a double document, but found " + type());
    }

    /**
     * Get the BigInteger value of the Document if it is a bigInteger.
     *
     * <p>If the value is a number of a different type, the value is cast, which can result in a loss of precision.
     *
     * @return the BigInteger value.
     * @throws SerializationException if the Document is not a number.
     */
    default BigInteger asBigInteger() {
        throw new SerializationException("Expected a bigInteger document, but found " + type());
    }

    /**
     * Get the BigDecimal value of the Document if it is a bigDecimal.
     *
     * <p>If the value is a number of a different type, the value is cast, which can result in a loss of precision.
     *
     * @return the BigDecimal value.
     * @throws SerializationException if the Document is not a number.
     */
    default BigDecimal asBigDecimal() {
        throw new SerializationException("Expected a bigDecimal document, but found " + type());
    }

    /**
     * Get the value of the builder as a Number.
     *
     * @return the Number value.
     * @throws SerializationException if the Document is not a numeric type.
     */
    default Number asNumber() {
        return switch (type()) {
            case BYTE -> asByte();
            case SHORT -> asShort();
            case INTEGER -> asInteger();
            case LONG -> asLong();
            case FLOAT -> asFloat();
            case DOUBLE -> asDouble();
            case BIG_INTEGER -> asBigInteger();
            case BIG_DECIMAL -> asBigDecimal();
            default -> throw new SerializationException("Expected a number document, but found " + type());
        };
    }

    /**
     * Get the string value of the Document if it is a string.
     *
     * @return the string value.
     * @throws SerializationException if the Document is not a string.
     */
    default String asString() {
        throw new SerializationException("Expected a string document, but found " + type());
    }

    /**
     * Get the Document as a blob if the Document is a blob.
     *
     * @return the bytes of the blob.
     * @throws SerializationException if the Document is not a blob.
     */
    default byte[] asBlob() {
        throw new SerializationException("Expected a blob document, but found " + type());
    }

    /**
     * Get the Document as an Instant if the Document is a timestamp.
     *
     * @return the Instant value of the timestamp.
     * @throws SerializationException if the Document is not a timestamp.
     */
    default Instant asTimestamp() {
        throw new SerializationException("Expected a timestamp document, but found " + type());
    }

    /**
     * Get the list contents of the Document if it is a list.
     *
     * @return the list contents.
     * @throws SerializationException if the Document is not a list.
     */
    default List<Document> asList() {
        throw new SerializationException("Expected a list document, but found " + type());
    }

    /**
     * Get the Document as a map of strings to Documents if the Document is a map, a structure, or union.
     *
     * <p>If the Document is a map, and the keys are strings the map entries are returned. If the document is a
     * structure or union, the members of the structure or union are returned as a map where the keys are the
     * member names of each present member.
     *
     * @return the map contents.
     * @throws SerializationException if the Document is not a map, structure, or union or the keys are numbers.
     */
    default Map<String, Document> asStringMap() {
        throw new SerializationException("Expected a map of strings to documents, but found " + type());
    }

    /**
     * Get a map, struct, or union member from the Document by name.
     *
     * @param memberName Member to access from the Document. For Document types with a schema, this name is the name
     *                   of the member defined in a Smithy model.
     * @return the member, or null if not found.
     * @throws IllegalStateException if the Document is not a string map, structure, or union shape.
     */
    default Document getMember(String memberName) {
        throw new SerializationException("Expected a map, structure, or union document, but found " + type());
    }

    /**
     * Create a normalized version of the document using normalized document types that aren't tied to any specific
     * protocol or schema.
     *
     * <p>A structure and union document are converted to a map of string to normalized documents. An intEnum is
     * converted to an integer. An enum is converted to a string.
     *
     * @return the normalized document.
     */
    default Document normalize() {
        return switch (type()) {
            case BOOLEAN -> createBoolean(asBoolean());
            case BYTE -> createByte(asByte());
            case SHORT -> createShort(asShort());
            case INTEGER, INT_ENUM -> createInteger(asInteger());
            case LONG -> createLong(asLong());
            case FLOAT -> createFloat(asFloat());
            case DOUBLE -> createDouble(asDouble());
            case BIG_INTEGER -> createBigInteger(asBigInteger());
            case BIG_DECIMAL -> createBigDecimal(asBigDecimal());
            case STRING, ENUM -> createString(asString());
            case BLOB -> createBlob(asBlob());
            case TIMESTAMP -> createTimestamp(asTimestamp());
            case STRUCTURE, UNION, MAP -> {
                var map = asStringMap();
                Map<String, Document> result = new LinkedHashMap<>(map.size());
                for (var entry : map.entrySet()) {
                    result.put(entry.getKey(), entry.getValue().normalize());
                }
                yield createStringMap(result);
            }
            case LIST -> {
                var list = asList();
                List<Document> result = new ArrayList<>(list.size());
                for (var element : list) {
                    result.add(element.normalize());
                }
                yield createList(result);
            }
            default -> throw new UnsupportedOperationException("Unable to normalize document: " + this);
        };
    }

    /**
     * Attempt to deserialize the Document into a builder.
     *
     * @param builder Builder to populate from the Document.
     * @param <T> Shape type to build.
     */
    default <T extends SerializableShape> void deserializeInto(ShapeBuilder<T> builder) {
        builder.deserialize(new DocumentDeserializer(this));
    }

    /**
     * Attempt to deserialize the Document into a builder and create the shape.
     *
     * @param builder Builder to populate from the Document.
     * @return the built and error-corrected shape.
     * @param <T> Shape type to build.
     */
    default <T extends SerializableShape> T asShape(ShapeBuilder<T> builder) {
        deserializeInto(builder);
        return builder.errorCorrection().build();
    }

    /**
     * Create a byte Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createByte(byte value) {
        return new Documents.ByteDocument(PreludeSchemas.BYTE, value);
    }

    /**
     * Create a short Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createShort(short value) {
        return new Documents.ShortDocument(PreludeSchemas.SHORT, value);
    }

    /**
     * Create an integer Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createInteger(int value) {
        return new Documents.IntegerDocument(PreludeSchemas.INTEGER, value);
    }

    /**
     * Create a long Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createLong(long value) {
        return new Documents.LongDocument(PreludeSchemas.LONG, value);
    }

    /**
     * Create a float Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createFloat(float value) {
        return new Documents.FloatDocument(PreludeSchemas.FLOAT, value);
    }

    /**
     * Create a double Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createDouble(double value) {
        return new Documents.DoubleDocument(PreludeSchemas.DOUBLE, value);
    }

    /**
     * Create a bigInteger Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createBigInteger(BigInteger value) {
        return new Documents.BigIntegerDocument(PreludeSchemas.BIG_INTEGER, value);
    }

    /**
     * Create a bigDecimal Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createBigDecimal(BigDecimal value) {
        return new Documents.BigDecimalDocument(PreludeSchemas.BIG_DECIMAL, value);
    }

    /**
     * Create a boolean Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createBoolean(boolean value) {
        return new Documents.BooleanDocument(PreludeSchemas.BOOLEAN, value);
    }

    /**
     * Create a string Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createString(String value) {
        return new Documents.StringDocument(PreludeSchemas.STRING, value);
    }

    /**
     * Create a blob Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createBlob(byte[] value) {
        return new Documents.BlobDocument(PreludeSchemas.BLOB, value);
    }

    /**
     * Create a timestamp Document.
     *
     * @param value Value to wrap.
     * @return the Document type.
     */
    static Document createTimestamp(Instant value) {
        return new Documents.TimestampDocument(PreludeSchemas.TIMESTAMP, value);
    }

    /**
     * Create a list Document.
     *
     * @param values Values to wrap.
     * @return the Document type.
     */
    static Document createList(List<Document> values) {
        return new Documents.ListDocument(Documents.LIST_SCHEMA, values);
    }

    /**
     * Create a Document for a map of strings to Documents.
     *
     * @param members Members to wrap.
     * @return the Document type.
     */
    static Document createStringMap(Map<String, Document> members) {
        return new Documents.StringMapDocument(Documents.STR_MAP_SCHEMA, members);
    }

    /**
     * Create a Document from a {@link SerializableShape}.
     *
     * <p>The created document is <em>typed</em> and captures the state of the shape exactly; meaning if the document
     * is serialized or deserialized by a codec, it'd done so exactly as if the underlying shape was serialized or
     * deserialized.
     *
     * @param shape Shape to turn into a Document.
     * @return the Document type.
     */
    static Document createTyped(SerializableShape shape) {
        var parser = new DocumentParser();
        shape.serialize(parser);
        return parser.getResult();
    }

    /**
     * Determines if two documents are equal, ignoring schemas and protocol details.
     *
     * @param left  Left document to compare.
     * @param right Right document to compare.
     * @return true if they are equal.
     */
    static boolean equals(Object left, Object right) {
        if (left instanceof Document l) {
            if (right instanceof Document r) {
                if (l == r) {
                    return true;
                } else if (l.type() != r.type()) {
                    return false;
                }
                return switch (l.type()) {
                    case BLOB -> Arrays.equals(l.asBlob(), r.asBlob());
                    case BOOLEAN -> l.asBoolean() == r.asBoolean();
                    case STRING, ENUM -> l.asString().equals(r.asString());
                    case TIMESTAMP -> l.asTimestamp().equals(r.asTimestamp());
                    case BYTE -> l.asByte() == r.asByte();
                    case SHORT -> l.asShort() == r.asShort();
                    case INTEGER, INT_ENUM -> l.asInteger() == r.asInteger();
                    case LONG -> l.asLong() == r.asLong();
                    case FLOAT -> l.asFloat() == r.asFloat();
                    case DOUBLE -> l.asDouble() == r.asDouble();
                    case BIG_DECIMAL -> l.asBigDecimal().equals(r.asBigDecimal());
                    case BIG_INTEGER -> l.asBigInteger().equals(r.asBigInteger());
                    case LIST, SET -> {
                        var ll = l.asList();
                        var rl = r.asList();
                        if (ll.size() != rl.size()) {
                            yield false;
                        }
                        for (int i = 0; i < ll.size(); i++) {
                            if (!equals(ll.get(i), rl.get(i))) {
                                yield false;
                            }
                        }
                        yield true;
                    }
                    case MAP, STRUCTURE, UNION -> {
                        var lm = l.asStringMap();
                        var rm = r.asStringMap();
                        if (lm.size() != rm.size()) {
                            yield false;
                        }
                        for (var entry : lm.entrySet()) {
                            if (!equals(entry.getValue(), rm.get(entry.getKey()))) {
                                yield false;
                            }
                        }
                        yield true;
                    }
                    default -> false; // unexpected type (DOCUMENT, MEMBER, OPERATION, SERVICE).
                };
            }
        }
        return false;
    }
}
