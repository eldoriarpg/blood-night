package de.eldoria.bloodnight.nodes.trigger;

import de.eldoria.bloodnight.nodes.base.execution.ExecutableChainNode;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.nodes.trigger.base.TriggerLock;

import java.util.Map;

/**
 * Base node for a trigger node.
 * <p>
 * A trigger node represents an executable chain node which gets triggered based on external events.
 * <p>
 * Nodes of this type represents the entry points into a {@link NodeContainer}
 *
 * @param <T> type of node
 */
public abstract non-sealed class TriggerNode<T extends TriggerNode<T, V>, V> extends ExecutableChainNode<T> {
    // Lock for the trigger node.
    private final TriggerLock lock = new TriggerLock();

    public TriggerNode() {
    }

    public TriggerNode(Map<String, Edge> input, EditorMeta meta) {
        super(input, meta);
    }

    /**
     * Inject the data into the trigger node.
     *
     * @param data data to set into the output.
     */
    protected abstract void inject(V data);

    /**
     * Triggers a trigger node.
     * <p>
     * This will first inject the data via {@link #inject(Object)} and call {@link #invoke(NodeContainer)} afterward.
     *
     * @param data data provided to the triggered node.
     */
    public final void trigger(NodeContainer container, Object data) {
        try (var l = lock.lock()) {
            inject((V) data);
            super.invoke(container);
        }
    }

    /**
     * Gets the output container of this node.
     * <p>
     * The output container can be only retrieved inside the {@link #inject(Object)} and {@link #invoke(NodeContainer)} call of this node.
     * <p>
     * Additionally, the output can be called from every node which is chained to the execution chain of this node in any way.
     * <p>
     * <b>Accessing this output container during a call from another trigger node will fail and throw an exception.</b>
     *
     * @return the output container
     * @throws IllegalStateException when this method is called outside the {@link #inject(Object)} or {@link #invoke(NodeContainer)} call of this node.
     */
    @Override
    public final OutputContainer output() {
        if (lock.locked()) {
            return output(super.output());
        }
        throw new IllegalStateException("Tried to get output from a trigger node which is not active");
    }

    /**
     * Refresh the output of this node
     *
     * @param output output container
     * @return this container with refreshed output
     */
    protected abstract OutputContainer output(OutputContainer output);
}
