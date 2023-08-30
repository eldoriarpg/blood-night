package de.eldoria.bloodnight.mob;

import de.eldoria.bloodnight.mob.meta.*;
import de.eldoria.bloodnight.nodes.container.ContainerMeta;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.util.Checks;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.Entity;

public record CustomMob(String id, Equipment equipment, Attributes attributes, NodeContainer nodes, MobDrops mobDrops,
                        Extension extension) {

    public CustomMob {
        Checks.notNull(extension, "Extension can not be null");
        Checks.notNull(id, "Mob id can not be null");
        Checks.lowerCase(id, "Mob id must be lower case.");
    }

    public CustomMob createLiving(Entity entity) {
        NodeContainer copy = nodes.copy();
        Entity extensionEntity = extension.type() != ExtensionType.NONE
                ? createExtension(entity)
                : null;
        MobTags.setupTags(entity, extensionEntity, this);

        // TODO: 21.08.23 apply other attributes

        copy.inject(new ContainerMeta(entity, extensionEntity));
        return new CustomMob(id, equipment, attributes, copy, mobDrops, this.extension);
    }

    private Entity createExtension(Entity baseEntity) {
        var world = baseEntity.getWorld();
        var extensionEntity = world.spawnEntity(baseEntity.getLocation(), extension.entityType());

        // TODO: 21.08.23 apply equipment

        boolean added = switch (extension.type()) {
            case RIDER -> extensionEntity.addPassenger(baseEntity);
            case CARRIER -> baseEntity.addPassenger(extensionEntity);
            case NONE -> throw new IllegalStateException("No NONE type expected here");
        };
        if (!added) throw new RuntimeException("Extension couldn't be added."); // TODO: 21.08.23 good logging
        return extensionEntity;
    }
}
