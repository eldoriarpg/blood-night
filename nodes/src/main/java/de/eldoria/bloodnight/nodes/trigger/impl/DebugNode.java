package de.eldoria.bloodnight.nodes.trigger.impl;

import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;

/**
 * Debugging node. Used for manual triggering of events.
 */
@Execution(Fields.NEXT)
public final class DebugNode extends TriggerNode<DebugNode> {
}
