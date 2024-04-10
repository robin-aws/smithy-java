/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.runtime.core.serde.document;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.java.runtime.core.schema.PreludeSchemas;
import software.amazon.smithy.java.runtime.core.schema.SdkSchema;
import software.amazon.smithy.java.runtime.core.serde.ShapeSerializer;
import software.amazon.smithy.java.runtime.core.serde.SpecificShapeSerializer;
import software.amazon.smithy.java.runtime.core.serde.StructSerializer;
import software.amazon.smithy.model.shapes.ShapeType;

public class StructDocumentTest {
    @Test
    public void createsDocument() {
        Map<String, Document> entries = new LinkedHashMap<>();
        entries.put("a", Document.of("a"));
        entries.put("b", Document.of(1));
        var document = Document.ofStruct(entries);

        assertThat(document.type(), equalTo(ShapeType.STRUCTURE));
        assertThat(document.getMember("a"), equalTo(Document.of("a")));
        assertThat(document.getMember("b"), equalTo(Document.of(1)));
    }

    @Test
    public void convertStructureToMap() {
        Map<String, Document> entries = new LinkedHashMap<>();
        entries.put("a", Document.of("a"));
        entries.put("b", Document.of(1));
        var document = Document.ofStruct(entries);

        assertThat(
            document.asMap(),
            equalTo(
                Map.of(
                    Document.of("a"),
                    Document.of("a"),
                    Document.of("b"),
                    Document.of(1)
                )
            )
        );
    }

    @Test
    public void serializesShape() {
        Map<String, Document> entries = new LinkedHashMap<>();
        entries.put("a", Document.of("a"));
        entries.put("b", Document.of(1));
        var document = Document.ofStruct(entries);

        document.serialize(new SpecificShapeSerializer() {
            @Override
            public void writeDocument(Document value) {
                assertThat(value, is(document));
            }
        });
    }

    @Test
    public void serializesContent() {
        Map<String, Document> entries = new LinkedHashMap<>();
        entries.put("a", Document.of("a"));
        entries.put("b", Document.of(1));
        var document = Document.ofStruct(entries);

        List<String> actions = new ArrayList<>();

        document.serializeContents(new SpecificShapeSerializer() {
            @Override
            public StructSerializer beginStruct(SdkSchema schema) {
                assertThat(schema, equalTo(PreludeSchemas.DOCUMENT));
                actions.add("beginStruct");
                return new StructSerializer() {
                    @Override
                    public void endStruct() {
                        actions.add("endStruct");
                    }

                    @Override
                    public void member(SdkSchema member, Consumer<ShapeSerializer> memberWriter) {
                        actions.add("member:" + member.memberName());
                        memberWriter.accept(new SpecificShapeSerializer() {
                            @Override
                            public void writeString(SdkSchema schema, String value) {
                                assertThat(schema, equalTo(PreludeSchemas.STRING));
                                actions.add("value:string:" + value);
                            }

                            @Override
                            public void writeInteger(SdkSchema schema, int value) {
                                assertThat(schema, equalTo(PreludeSchemas.INTEGER));
                                actions.add("value:integer:" + value);
                            }
                        });
                    }
                };
            }
        });

        assertThat(
            actions,
            containsInAnyOrder(
                "beginStruct",
                "endStruct",
                "member:a",
                "member:b",
                "value:string:a",
                "value:integer:1"
            )
        );
    }
}
