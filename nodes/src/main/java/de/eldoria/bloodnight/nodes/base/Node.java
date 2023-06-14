package de.eldoria.bloodnight.nodes.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.execution.ExecutableNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.InputContainer;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.base.io.Vec2D;
import de.eldoria.bloodnight.nodes.serializastion.InputContainerSerializer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import de.eldoria.bloodnight.nodes.value.ValueNode;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a base node. This node holds the input and output values of a node and the meta for editors.
 * <p>
 * See {@link ExecutableNode}, {@link ValueNode} and {@link TransformNode} for specific implementation details.
 */
@JsonTypeInfo(visible = true, property = "nodeClass", use = JsonTypeInfo.Id.CLASS)
@JsonPropertyOrder(alphabetic = true)
@JsonIgnoreProperties({"nodeClass"})
public sealed abstract class Node permits ExecutableNode, ValueNode, TransformNode {
    // Only serialize edges
    @JsonSerialize(using = InputContainerSerializer.class)
    private final InputContainer input;
    // output does not need to be serialized.
    @JsonIgnore
    private final OutputContainer output;
    @JsonSerialize
    private final EditorMeta meta;

    public Node(Map<String, Edge> input, EditorMeta meta) {
        this.input = new InputContainer(MetadataReader.readInputs(this.getClass()), input);
        this.output = new OutputContainer(MetadataReader.readOutputs(this.getClass()));
        this.meta = meta;
    }

    public Node() {
        input = new InputContainer(MetadataReader.readInputs(this.getClass()));
        output = new OutputContainer(MetadataReader.readOutputs(this.getClass()));
        this.meta = new EditorMeta(new Vec2D(0, 0), null);
    }

    /**
     * Get the input container of this node.
     *
     * @return input container
     */
    public InputContainer input() {
        return input;
    }

    /**
     * Get the input container of this node.
     *
     * @return input container
     */
    public Node input(Consumer<InputContainer> consumer) {
        consumer.accept(input);
        return this;
    }

    /**
     * Get the output container of this node. Calling this will eventually refresh the values provided by the node.
     *
     * @param container container holding the nodes
     * @return output container
     */
    public OutputContainer output(NodeContainer container) {
        return output;
    }

    protected OutputContainer output() {
        return output;
    }

    /**
     * Creates a new instance of this node, which can be used for another mob instance.
     * <p>
     * For optimization reasons, this might be only implemented into nodes which actually hold data. Nodes which do not store any data or just static data might return themselves.
     *
     * @return a copy of this node if it holds any persistent data.
     */
    // TODO make this abstract to force implementation or are we fine with the non static data approach?
    public Node copy() {
        return this;
    }
}

