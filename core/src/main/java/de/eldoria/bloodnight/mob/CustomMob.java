package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.mobs.meta.Attributes;
import de.eldoria.bloodnight.mobs.meta.Drops;
import de.eldoria.bloodnight.mobs.meta.Equipment;
import de.eldoria.bloodnight.mobs.meta.Extension;
import de.eldoria.bloodnight.nodes.container.NodeContainer;

public record CustomMob(String id, Equipment equipment, Attributes attributes, NodeContainer nodes, Drops drops, Extension extension) {
}
