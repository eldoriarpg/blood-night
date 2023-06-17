package de.eldoria.bloodnight.nodes.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.eldoria.bloodnight.nodes.base.execution.ExecutableNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.InputContainer;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.base.io.Vec2D;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.meta.MetadataReader;
import de.eldoria.bloodnight.nodes.provider.ProviderNode;
import de.eldoria.bloodnight.nodes.serialization.InputContainerSerializer;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import de.eldoria.bloodnight.nodes.value.ValueNode;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
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
public sealed abstract class Node permits ExecutableNode, ProviderNode, TransformNode, ValueNode {
    @JsonIgnore
    private MethodHandle constructorHandle;
    @JsonIgnore
    private NodeContainer container;
    @JsonSerialize
    @JsonProperty(defaultValue = "-1")
    private int id = -1;
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
     * @return output container
     */
    public OutputContainer output() {
        return output;
    }

    protected final NodeContainer container() {
        return container;
    }

    @ApiStatus.Internal
    public void inject(int id, NodeContainer container) {
        if (this.id != -1) throw new IllegalStateException("Node id is already set up");
        this.id = id;
        this.container = container;
        input.inject(container);
    }

    public EditorMeta meta() {
        return meta;
    }

    /**
     * Creates a new instance of this node, which can be used for another mob instance.
     * <p>
     * For optimization reasons, this might be only implemented into nodes which actually hold data. Nodes which do not store any data or just static data might return themselves.
     * <p>
     * This node is an uninjected node meaning that the {@link #inject(int, NodeContainer)} method needs to be called afterward.
     *
     * @return a copy of this node if it holds any persistent data.
     */
    public Node copy() {
        if (constructorHandle == null) {
            try {
                var lookup = MethodHandles.publicLookup();
                MethodType mt = MethodType.methodType(void.class, List.of(Map.class, EditorMeta.class));
                constructorHandle = lookup.findConstructor(getClass(), mt);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return (Node) constructorHandle.invoke(new HashMap<>(input.edges()), meta);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}

