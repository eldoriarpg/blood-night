package de.eldoria.bloodnight.nodes.base;

import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.execution.ExecutableNode;
import de.eldoria.bloodnight.nodes.base.io.InputContainer;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.value.ValueNode;
import de.eldoria.bloodnight.nodes.transform.TransformNode;

public sealed abstract class Node permits ExecutableNode, ValueNode, TransformNode {
    private final InputContainer input = new InputContainer(MetadataReader.readInputs(this.getClass()));
    private final OutputContainer output = new OutputContainer(MetadataReader.readOutputs(this.getClass()));

    /**
     * Get the input container of this node.
     * @return input container
     */
    public InputContainer input() {
        return input;
    }

    /**
     * Get the output container of this node. Calling this will eventually refresh the values provided by the node.
     * @param container container holding the nodes
     * @return output container
     */
    public OutputContainer output(NodeContainer container) {
        return output;
    }

}

