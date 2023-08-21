package de.eldoria.bloodnight.mob;

import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.Extension;
import de.eldoria.bloodnight.nodes.container.ContainerMeta;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.util.Checks;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.persistence.PersistentDataType;

public record CustomMob(String id, Equipment equipment, Attributes attributes, NodeContainer nodes, MobDrops mobDrops,
                        Extension extension) {

    public CustomMob {
        Checks.notNull(id, "Mob id can not be null");
        Checks.lowerCase(id, "Mob id must be lower case.");
    }

    public CustomMob createLiving(Entity entity) {
        MobTags.markCustomMob(entity);

        NodeContainer copy = nodes.copy();
        Entity extensionEntity = createExtension(entity);

        // TODO: 21.08.23 apply other attributes

        copy.inject(new ContainerMeta(entity, extensionEntity));
        return new CustomMob(id, equipment, attributes, copy, mobDrops, this.extension);
    }

    private Entity createExtension(Entity baseEntity) {
        if (extension == null) return null;
        var world = baseEntity.getWorld();
        var extensionEntity = world.spawnEntity(baseEntity.getLocation(), extension.entityType());
        MobTags.markExtension(extensionEntity, baseEntity);
        MobTags.markExtendedMob(baseEntity, extensionEntity);

        // TODO: 21.08.23 apply equipment

        boolean added = switch (extension.type()) {
            case RIDER -> extensionEntity.addPassenger(baseEntity);
            case CARRIER -> baseEntity.addPassenger(extensionEntity);
        };
        if (!added) throw new RuntimeException("Extension couldn't be added."); // TODO: 21.08.23 good logging
        return extensionEntity;
    }
}
