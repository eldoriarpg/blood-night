package de.eldoria.bloodnight.nodes.trigger.impl;

import de.eldoria.bloodnight.nodes.annotations.NodeMeta;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;

/**
 * Tick node is executed every server tick. Approx. every 50 ms.
 */
@NodeMeta(name = "Tick", description = "Executed on every server tick")
public class TickNode extends TriggerNode<TickNode> {
}
