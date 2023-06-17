package de.eldoria.bloodnight.nodes.transform;

import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;

import java.util.Map;

/**
 * Base node for a transform node.
 * <p>
 * A transform node transforms one or more inputs to one or more outputs.
 */
public abstract non-sealed class TransformNode extends Node {
    public TransformNode(Map<String, Edge> input, EditorMeta meta) {
        super(input, meta);
    }

    public TransformNode() {
    }
}
