package de.eldoria.bloodnight.nodes.base;

import de.eldoria.bloodnight.nodes.MetadataReader;
import de.eldoria.bloodnight.nodes.NodeContainer;

public abstract class Node {
    private final InputContainer input = new InputContainer(MetadataReader.readInputs(this.getClass()));
    private final OutputContainer output = new OutputContainer(MetadataReader.readOutputs(this.getClass()));

    public InputContainer input() {
        return input;
    }

    public OutputContainer output(NodeContainer container) {
        return output;
    }

}

