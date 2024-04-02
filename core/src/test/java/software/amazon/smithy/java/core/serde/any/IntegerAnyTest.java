/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.core.serde.any;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.java.runtime.core.schema.SdkSchema;
import software.amazon.smithy.java.runtime.core.serde.SdkSerdeException;
import software.amazon.smithy.java.runtime.core.serde.ShapeSerializer;
import software.amazon.smithy.java.runtime.core.serde.SpecificShapeSerializer;
import software.amazon.smithy.java.runtime.core.serde.any.Any;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ShapeType;

public class IntegerAnyTest {

    @Test
    public void createsAnyWithoutSchema() {
        var any = Any.of(10);

        assertThat(any.type(), equalTo(ShapeType.INTEGER));
        assertThat(any.schema().id(), equalTo(Any.SCHEMA.id()));
        assertThat(any.asInteger(), equalTo(10));
        assertThat(any, equalTo(Any.of(10)));
    }

    @Test
    public void validatesSchemaType() {
        Assertions.assertThrows(SdkSerdeException.class, () -> {
            Any.of(1, SdkSchema.builder().id("smithy.example#Shape").type(ShapeType.BYTE).build());
        });
    }

    @Test
    public void createsAnyWithSchema() {
        var targetSchema = SdkSchema.builder()
            .type(ShapeType.INTEGER)
            .id(ShapeId.from("smithy.example#Shape"))
            .build();
        var schema = SdkSchema.memberBuilder(0, "mymember", targetSchema)
            .id(targetSchema.id())
            .build();
        var any = Any.of(10, schema);

        assertThat(any.type(), equalTo(ShapeType.INTEGER));
        assertThat(any.schema(), equalTo(schema));
        assertThat(any.asInteger(), equalTo(10));
        assertThat(any, equalTo(Any.of(10, schema)));
    }

    @Test
    public void serializesShape() {
        var targetSchema = SdkSchema.builder()
            .type(ShapeType.INTEGER)
            .id(ShapeId.from("smithy.example#Shape"))
            .build();
        var schema = SdkSchema.memberBuilder(0, "mymember", targetSchema)
            .id(targetSchema.id())
            .build();

        var any = Any.of(10, schema);

        ShapeSerializer serializer = new SpecificShapeSerializer() {
            @Override
            protected RuntimeException throwForInvalidState(SdkSchema schema) {
                return new RuntimeException("Unexpected " + schema);
            }

            @Override
            public void writeInteger(SdkSchema schema, int value) {
                assertThat(schema, equalTo(schema));
                assertThat(value, equalTo(10));
            }
        };

        any.serialize(serializer);
    }

    @Test
    public void canWidenType() {
        var any = Any.of(10);

        assertThat(any.asLong(), equalTo(10L));
        assertThat(any.asFloat(), equalTo(10f));
        assertThat(any.asDouble(), equalTo(10.0));
    }
}
