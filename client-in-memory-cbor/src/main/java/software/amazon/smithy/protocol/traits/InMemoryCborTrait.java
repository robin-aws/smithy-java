package software.amazon.smithy.protocol.traits;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.AbstractTraitBuilder;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.utils.ToSmithyBuilder;

public class InMemoryCborTrait extends AbstractTrait implements ToSmithyBuilder<InMemoryCborTrait> {

    public static final ShapeId ID = ShapeId.from("smithy.protocols#inmemoryv1Cbor");

    private InMemoryCborTrait(InMemoryCborTrait.Builder builder) {
        super(ID, builder.getSourceLocation());
    }

    /**
     * Creates a new {@code Builder}.
     */
    public static InMemoryCborTrait.Builder builder() {
        return new InMemoryCborTrait.Builder();
    }

    /**
     * Updates the builder from a Node.
     *
     * @param node Node object that must be a valid {@code ObjectNode}.
     * @return Returns the updated builder.
     */
    public static InMemoryCborTrait fromNode(Node node) {
        InMemoryCborTrait.Builder builder = builder().sourceLocation(node);
//        ObjectNode objectNode = node.expectObjectNode();
        return builder.build();
    }

    @Override
    protected Node createNode() {
        ObjectNode.Builder builder = Node.objectNodeBuilder().sourceLocation(getSourceLocation());
        return builder.build();
    }

    @Override
    public InMemoryCborTrait.Builder toBuilder() {
        return builder();
    }

    /**
     * Builder for creating a {@code Rpcv2CborTrait}.
     */
    public static final class Builder extends AbstractTraitBuilder<InMemoryCborTrait, InMemoryCborTrait.Builder> {

        @Override
        public InMemoryCborTrait build() {
            return new InMemoryCborTrait(this);
        }
    }

    /**
     * Implements the {@code AbstractTrait.Provider}.
     */
    public static final class Provider extends AbstractTrait.Provider {

        public Provider() {
            super(ID);
        }

        @Override
        public Trait createTrait(ShapeId target, Node value) {
            InMemoryCborTrait result = fromNode(value);
            result.setNodeCache(value);
            return result;
        }
    }
}
