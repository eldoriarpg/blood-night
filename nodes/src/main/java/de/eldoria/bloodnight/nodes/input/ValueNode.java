package de.eldoria.bloodnight.nodes.input;

import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.NodeContainer;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;

public abstract non-sealed class ValueNode extends Node {
    private final OutputContainer outputContainer = new OutputContainer(MetadataReader.readOutputs(this.getClass()));

    public ValueNode() {
    }

    public ValueNode(Object value) {
        outputContainer.set(Fields.VALUE, value);
    }

    @Override
    public OutputContainer output(NodeContainer container) {
        return outputContainer;
    }
}
