package de.eldoria.bloodnight.nodes.container;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.dispatching.TriggerData;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A container holding a bunch of nodes.
 */
public final class NodeContainer {
    @JsonProperty
    private final Map<Integer, Node> nodes;
    @JsonIgnore
    private final Map<Class<? extends Node>, List<TriggerNode<?, ?>>> trigger = new HashMap<>();
    @JsonIgnore
    private ContainerMeta meta;

    public NodeContainer() {
        this.nodes = new HashMap<>();
    }

    @JsonCreator
    public NodeContainer(@JsonProperty("nodes") Map<Integer, Node> nodes) {
        this.nodes = nodes;
    }

    public <T extends Node> T add(int id, T node) {
        if (nodes.containsKey(id)) {
            throw new IllegalArgumentException("A node with id %s already exists!".formatted(id));
        }
        nodes.put(id, node);
        node.inject(id, this);
        return node;
    }

    public <V> void dispatch(TriggerData<V> data) {
        if (trigger.isEmpty()) {
            buildTriggerMap();
            if (trigger.isEmpty()) throw new IllegalStateException("Node container does not contain any trigger nodes");
        }

        for (TriggerNode<?, ?> triggerNode : trigger.get(data.triggerNodeClass())) {
            triggerNode.trigger(this, data.data());
        }
    }

    private void buildTriggerMap() {
        for (Node value : nodes.values()) {
            if (value instanceof TriggerNode<?, ?> t) {
                trigger.putIfAbsent(t.getClass(), new ArrayList<>()).add(t);
            }
        }
    }

    public Node get(int id) {
        return Objects.requireNonNull(nodes.get(id));
    }

    public NodeContainer copy() {
        NodeContainer nodeContainer = new NodeContainer();
        for (var entry : nodes.entrySet()) {
            nodeContainer.add(entry.getKey(), entry.getValue().copy());
        }
        return nodeContainer;
    }

    public void inject(ContainerMeta meta){
        this.meta = meta;
    }

    public ContainerMeta meta() {
        return meta;
    }
}
