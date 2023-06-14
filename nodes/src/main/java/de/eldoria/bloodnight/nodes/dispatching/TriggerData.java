package de.eldoria.bloodnight.nodes.dispatching;

import de.eldoria.bloodnight.nodes.trigger.TriggerNode;

public record TriggerData<V>(Class<? extends TriggerNode<?, V>> triggerNodeClass, V data) {
}
