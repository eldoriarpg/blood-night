package de.eldoria.bloodnight.nodes.dispatching;

import de.eldoria.bloodnight.nodes.trigger.TriggerNode;

/**
 * This class holds the trigger data for a {@link TriggerNode}.
 *
 * @param triggerNodeClass the class of the node to be triggered
 * @param data             the data required by the trigger node
 * @param <V>              the type of the data required by the trigger node
 */
public record TriggerData<V>(Class<? extends TriggerNode<?, V>> triggerNodeClass, V data) {

    /**
     * Creates a new TriggerData instance for the specified TriggerNode class using the provided data.
     *
     * @param triggerNodeClass The class of the TriggerNode.
     * @param data             The data to be associated with the TriggerNode.
     * @param <V>              The type of the data.
     * @return A new TriggerData instance.
     */
    public static <V> TriggerData<V> forNode(Class<? extends TriggerNode<?, V>> triggerNodeClass, V data) {
        return new TriggerData<>(triggerNodeClass, data);
    }
}
