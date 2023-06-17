package de.eldoria.bloodnight.mob;

import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.Drops;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.Extension;
import de.eldoria.bloodnight.nodes.container.NodeContainer;

public record CustomMob(String id, Equipment equipment, Attributes attributes, NodeContainer nodes, Drops drops,
                        Extension extension) {
}
