package de.eldoria.bloodnight.mob;

import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.meta.*;
import de.eldoria.bloodnight.nodes.container.ContainerMeta;
import de.eldoria.bloodnight.nodes.container.NodeContainer;
import de.eldoria.bloodnight.util.Checks;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public record CustomMob(String id, Equipment equipment, Attributes attributes, NodeContainer nodes, MobDrops mobDrops,
                        Extension extension) {

    public CustomMob {
        Checks.notNull(extension, "Extension can not be null");
        Checks.notNull(id, "Mob id can not be null");
        Checks.lowerCase(id, "Mob id must be lower case.");
    }

    public CustomMob createLiving(LivingEntity entity, Configuration configuration) {
        NodeContainer copy = nodes.copy();
        Entity extensionEntity = extension.type() != ExtensionType.NONE
                ? createExtension(entity, configuration)
                : null;
        MobTags.setupTags(entity, extensionEntity, this);

        applyEquipment(entity, this.equipment, configuration);

        copy.inject(new ContainerMeta(entity, extensionEntity));
        return new CustomMob(id, equipment, attributes, copy, mobDrops, this.extension);
    }

    private Entity createExtension(Entity baseEntity, Configuration configuration) {
        var world = baseEntity.getWorld();
        var extensionEntity = world.spawnEntity(baseEntity.getLocation(), extension.entityType());

        if (!(extensionEntity instanceof LivingEntity livingEntity)) throw new RuntimeException("Entity is no living entity");

        applyEquipment(livingEntity, equipment, configuration);

        boolean added = switch (extension.type()) {
            case RIDER -> extensionEntity.addPassenger(baseEntity);
            case CARRIER -> baseEntity.addPassenger(extensionEntity);
            case NONE -> throw new IllegalStateException("No NONE type expected here");
        };
        if (!added) throw new RuntimeException("Extension couldn't be added."); // TODO: 21.08.23 good logging
        return extensionEntity;
    }

    private ItemStack getItem(String item, Configuration configuration) {
        return configuration.items().registrations().get(item);
    }

    private void applyEquipment(LivingEntity entity, Equipment equipment, Configuration configuration) {
        var entityEquipment = entity.getEquipment();
        if (entityEquipment == null) return;
        entityEquipment.setBoots(getItem(equipment.feet(), configuration));
        entityEquipment.setLeggings(getItem(equipment.legs(), configuration));
        entityEquipment.setChestplate(getItem(equipment.chest(), configuration));
        entityEquipment.setHelmet(getItem(equipment.head(), configuration));
        entityEquipment.setItemInMainHand(getItem(equipment.mainHand(), configuration));
        entityEquipment.setItemInOffHand(getItem(equipment.offHand(), configuration));
    }
}
