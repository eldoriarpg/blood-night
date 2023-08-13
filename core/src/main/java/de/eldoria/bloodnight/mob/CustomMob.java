package de.eldoria.bloodnight.mob;

import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.Extension;
import de.eldoria.bloodnight.nodes.container.ContainerMeta;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import org.bukkit.entity.Entity;

public record CustomMob(String id, Equipment equipment, Attributes attributes, NodeContainer nodes, MobDrops mobDrops,
                        Extension extension) {
    public CustomMob copy(Entity entity, Entity extension) {
        NodeContainer copy = nodes.copy();
        copy.inject(new ContainerMeta(entity, extension));
        return new CustomMob(id, equipment, attributes, copy, mobDrops, this.extension);
    }
}
