/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.dynamicclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import software.amazon.smithy.java.core.schema.Schema;
import software.amazon.smithy.java.core.schema.SchemaUtils;
import software.amazon.smithy.java.core.schema.ShapeBuilder;
import software.amazon.smithy.java.core.serde.ShapeDeserializer;
import software.amazon.smithy.java.core.serde.document.Document;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ShapeType;

final class SchemaGuidedDocumentBuilder implements ShapeBuilder<WrappedDocument> {

    private final ShapeId service;
    private final Schema target;
    private Document result;
    private final Map<String, Document> map;

    SchemaGuidedDocumentBuilder(ShapeId service, Schema target) {
        this.service = service;
        this.target = target;
        this.map = switch (target.type()) {
            case STRUCTURE, UNION, MAP -> new HashMap<>();
            default -> null;
        };
    }

    @Override
    public Schema schema() {
        return target;
    }

    @Override
    public WrappedDocument build() {
        if (map != null) {
            if (map.isEmpty() && target.type() == ShapeType.UNION) {
                throw new IllegalArgumentException("No value set for union document: " + schema().id());
            }
            return new WrappedDocument(service, target, Document.of(map));
        } else if (result != null) {
            return new WrappedDocument(service, target, result);
        } else {
            throw new IllegalArgumentException("No value was set on document builder for " + schema().id());
        }
    }

    @Override
    public void setMemberValue(Schema member, Object value) {
        if (map != null) {
            SchemaUtils.validateMemberInSchema(target, member, value);
            map.put(member.memberName(), Document.ofObject(value));
        } else {
            ShapeBuilder.super.setMemberValue(member, value);
        }
    }

    @Override
    public ShapeBuilder<WrappedDocument> deserialize(ShapeDeserializer decoder) {
        if (map != null) {
            map.putAll(deserialize(decoder, target).asStringMap());
        } else {
            result = deserialize(decoder, target);
        }
        return this;
    }

    @Override
    public ShapeBuilder<WrappedDocument> deserializeMember(ShapeDeserializer decoder, Schema schema) {
        if (map != null) {
            map.putAll(deserialize(decoder, schema.assertMemberTargetIs(target)).asStringMap());
        } else {
            result = deserialize(decoder, schema.assertMemberTargetIs(target));
        }
        return this;
    }

    private Document deserialize(ShapeDeserializer decoder, Schema schema) {
        return switch (schema.type()) {
            case BLOB -> Document.of(decoder.readBlob(target));
            case BOOLEAN -> Document.of(decoder.readBoolean(target));
            case STRING, ENUM -> Document.of(decoder.readString(target));
            case TIMESTAMP -> Document.of(decoder.readTimestamp(target));
            case BYTE -> Document.of(decoder.readByte(target));
            case SHORT -> Document.of(decoder.readShort(target));
            case INTEGER, INT_ENUM -> Document.of(decoder.readInteger(target));
            case LONG -> Document.of(decoder.readLong(target));
            case FLOAT -> Document.of(decoder.readFloat(target));
            case DOCUMENT -> decoder.readDocument();
            case DOUBLE -> Document.of(decoder.readDouble(target));
            case BIG_DECIMAL -> Document.of(decoder.readBigDecimal(target));
            case BIG_INTEGER -> Document.of(decoder.readBigInteger(target));
            case LIST -> {
                var items = new SchemaList(schema.listMember());
                decoder.readList(target, items, (it, memberDeserializer) -> {
                    it.add(deserialize(memberDeserializer, it.schema));
                });
                yield Document.of(items);
            }
            case MAP -> {
                var map = new SchemaMap(schema);
                decoder.readStringMap(schema, map, (state, mapKey, memberDeserializer) -> {
                    state.put(mapKey, deserialize(memberDeserializer, state.schema.mapValueMember()));
                });
                yield Document.of(map);
            }
            case STRUCTURE, UNION -> {
                var map = new HashMap<String, Document>();
                decoder.readStruct(schema, map, (state, memberSchema, memberDeserializer) -> {
                    state.put(memberSchema.memberName(), deserialize(memberDeserializer, memberSchema));
                });
                yield Document.of(map);
            }
            default -> throw new UnsupportedOperationException("Unsupported target type: " + target.type());
        };
    }

    @Override
    public ShapeBuilder<WrappedDocument> errorCorrection() {
        // TODO: fill in defaults.
        return this;
    }

    // Captures the schema of a list to pass to a closure.
    private static final class SchemaList extends ArrayList<Document> {
        private final Schema schema;

        SchemaList(Schema schema) {
            this.schema = schema;
        }
    }

    // Captures the schema of a map to pass to a closure.
    private static final class SchemaMap extends HashMap<String, Document> {
        private final Schema schema;

        SchemaMap(Schema schema) {
            this.schema = schema;
        }
    }
}
