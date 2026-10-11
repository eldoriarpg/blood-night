package de.eldoria.graph.compiler;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;

import java.util.List;
import java.util.Map;

/**
 * A graph whose structure and types have been checked, with everything the emitter needs resolved per node.
 *
 * @param platform the platform it was checked against
 * @param nodes    every node by instance id
 * @param entries  ids of entry nodes, in id order
 * @param warnings problems that do not prevent compilation
 */
record ValidatedGraph(GraphPlatform platform, Map<String, Node> nodes, List<String> entries, List<GraphProblem> warnings) {

    /**
     * One checked node instance.
     *
     * @param id          instance id
     * @param instance    the node as stored in the graph
     * @param definition  its definition
     * @param inputTypes  type of each declared input, type variables resolved
     * @param outputTypes type of each declared output; may still contain variables that could not be inferred
     * @param config      literal for every config field, defaults applied
     */
    record Node(String id,
                NodeInstance instance,
                NodeDefinition definition,
                Map<String, PortType> inputTypes,
                Map<String, PortType> outputTypes,
                Map<String, CodeBlock> config) {
    }
}
