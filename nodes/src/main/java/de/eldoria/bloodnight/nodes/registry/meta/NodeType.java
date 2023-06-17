package de.eldoria.bloodnight.nodes.registry.meta;

import de.eldoria.bloodnight.nodes.action.ActionNode;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.controlflow.ControlFlowNode;
import de.eldoria.bloodnight.nodes.transform.TransformNode;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;
import de.eldoria.bloodnight.nodes.value.ValueNode;

public enum NodeType {
    ACTION(ActionNode.class),
    TRIGGER(TriggerNode.class),
    TRANSFORM(TransformNode.class),
    CONTROL_FLOW(ControlFlowNode.class),
    VALUE(ValueNode.class);

    private final Class<? extends Node> nodeClass;

    NodeType(Class<? extends Node> nodeClass) {
        this.nodeClass = nodeClass;
    }

    public static NodeType getNodeType(Class<? extends Node> nodeClass) {
        for (NodeType value : values()) {
            if (value.nodeClass.isAssignableFrom(nodeClass)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Class %s does not inherit from a valid node class".formatted(nodeClass.getName()));
    }

    public Class<? extends Node> nodeClass() {
        return nodeClass;
    }
}
