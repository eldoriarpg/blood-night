package de.eldoria.bloodnight.nodes.trigger;

import de.eldoria.bloodnight.nodes.base.execution.ExecutableChainNode;

/**
 * Base node for a trigger node.
 *
 * A trigger node represents an executable chain node which gets triggered based on external events.
 * @param <T>
 */
public abstract non-sealed class TriggerNode<T extends TriggerNode<T>> extends ExecutableChainNode<T> {

}
