package de.eldoria.bloodnight.nodes.trigger.impl;

import de.eldoria.bloodnight.nodes.Categories;
import de.eldoria.bloodnight.nodes.Fields;
import de.eldoria.bloodnight.nodes.annotations.Execution;
import de.eldoria.bloodnight.nodes.annotations.Meta;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;

/**
 * Debugging node. Used for manual triggering of events.
 */
@Execution(Fields.NEXT)
@Meta(name = "Debug", description = "Manually trigger a action", category = Categories.DEBUGGING)
public final class DebugNode extends TriggerNode<DebugNode> {
}
