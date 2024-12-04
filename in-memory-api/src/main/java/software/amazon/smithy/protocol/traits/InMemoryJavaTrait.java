package software.amazon.smithy.protocol.traits;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.AbstractTraitBuilder;
import software.amazon.smithy.utils.ToSmithyBuilder;

public class InMemoryJavaTrait extends AbstractTrait implements ToSmithyBuilder<InMemoryJavaTrait> {

    public static final ShapeId ID = ShapeId.from("smithy.protocols#inmemoryv1Java");

    private InMemoryJavaTrait(InMemoryJavaTrait.Builder builder) {
        super(ID, builder.getSourceLocation());
    }

    /**
     * Creates a new {@code Builder}.
     */
    public static InMemoryJavaTrait.Builder builder() {
        return new InMemoryJavaTrait.Builder();
    }

    /**
     * Updates the builder from a Node.
     *
     * @param node Node object that must be a valid {@code ObjectNode}.
     * @return Returns the updated builder.
     */
    public static InMemoryJavaTrait fromNode(Node node) {
        InMemoryJavaTrait.Builder builder = builder().sourceLocation(node);
//        ObjectNode objectNode = node.expectObjectNode();
        return builder.build();
    }

    @Override
    protected Node createNode() {
        ObjectNode.Builder builder = Node.objectNodeBuilder().sourceLocation(getSourceLocation());
        return builder.build();
    }

    @Override
    public InMemoryJavaTrait.Builder toBuilder() {
        return builder();
    }

    /**
     * Builder for creating a {@code InMemoryJavaTrait}.
     */
    public static final class Builder extends AbstractTraitBuilder<InMemoryJavaTrait, InMemoryJavaTrait.Builder> {

        @Override
        public InMemoryJavaTrait build() {
            return new InMemoryJavaTrait(this);
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
        public InMemoryJavaTrait createTrait(ShapeId target, Node value) {
            InMemoryJavaTrait result = fromNode(value);
            result.setNodeCache(value);
            return result;
        }
    }
}
