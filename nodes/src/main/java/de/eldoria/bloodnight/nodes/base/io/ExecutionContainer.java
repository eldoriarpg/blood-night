package de.eldoria.bloodnight.nodes.base.io;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.base.execution.ExecutableNode;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Container holding the defined {@link Execution}s on a node.
 */
public final class ExecutionContainer {
    @JsonProperty
    private final Map<String, Set<Integer>> nextNodes;

    /**
     * Constructs a new execution container based on a map of nodes.
     * <p>
     * Use {@link MetadataReader#readExecutions(Class)} to create such a map from a class.
     *
     * @param nextNodes next nodes
     */
    @JsonCreator
    public ExecutionContainer(@JsonProperty("nextNodes") Map<String, Set<Integer>> nextNodes) {
        this.nextNodes = nextNodes;
    }

    /**
     * Checks whether the container is empty.
     *
     * @return true if empty
     */
    @JsonIgnore
    public boolean isEmpty() {
        return nextNodes.isEmpty();
    }

    /**
     * Get an unmodifiable set of names of the execution fields.
     *
     * @return unmodifiable set of names
     */
    public Set<String> names() {
        return Collections.unmodifiableSet(nextNodes.keySet());
    }

    /**
     * Get the ids of the next nodes on an execution field.
     *
     * @param out name of the node
     * @return set of integers
     */
    public Set<Integer> next(String out) {
        return nextNodes.get(out);
    }

    /**
     * Get the next {@link ExecutableNode}s from a container which are linked to this container.
     *
     * @param container container containing the linked nodes
     * @param name      name of the executable field
     * @return list of {@link ExecutableNode}s
     */
    public Collection<ExecutableNode> nextNodes(NodeContainer container, String name) {
        return next(name)
                .stream()
                .map(container::get)
                .map(ExecutableNode.class::cast)
                .toList();

    }

    /**
     * Calls the {@link ExecutableNode#invoke(NodeContainer)} method on a {@link NodeContainer} for all containers linked to the execution field.
     *
     * @param container container holding next nodes
     * @param name      name of the execution field
     */
    public void invokeNext(NodeContainer container, String name) {
        for (var node : nextNodes(container, name)) {
            node.invoke(container);
        }
    }

    /**
     * Checks whether a name is set inside this container
     *
     * @param name name
     * @return true if set
     */
    public boolean hasName(String name) {
        return nextNodes.containsKey(name);
    }
}
