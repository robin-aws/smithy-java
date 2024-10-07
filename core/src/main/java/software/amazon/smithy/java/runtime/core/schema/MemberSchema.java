/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.java.runtime.core.schema;

import java.util.List;
import java.util.Set;
import software.amazon.smithy.model.traits.Trait;

/**
 * A member schema that targets a built shape.
 */
final class MemberSchema extends Schema {

    private final Schema target;
    private final TraitMap directTraits;

    MemberSchema(MemberSchemaBuilder builder) {
        super(builder);
        this.target = builder.target;
        this.directTraits = builder.directTraits;
    }

    @Override
    public Schema memberTarget() {
        return target;
    }

    @Override
    public Schema member(String memberName) {
        return target.member(memberName);
    }

    @Override
    public List<Schema> members() {
        return target.members();
    }

    @Override
    public Set<String> stringEnumValues() {
        return target.stringEnumValues();
    }

    @Override
    public Set<Integer> intEnumValues() {
        return target.intEnumValues();
    }

    @Override
    public <T extends Trait> T getDirectTrait(Class<T> trait) {
        return directTraits.get(trait);
    }
}
