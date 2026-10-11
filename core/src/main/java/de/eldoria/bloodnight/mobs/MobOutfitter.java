/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.ExtensionType;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Turns a spawned entity into a custom mob: equipment, extension entity and tags.
 */
public final class MobOutfitter {
    private final Supplier<ItemRegistry> items;

    /**
     * @param items the current item registry
     */
    public MobOutfitter(Supplier<ItemRegistry> items) {
        this.items = items;
    }

    /**
     * @param entity the entity becoming the mob
     * @param mob    the mob definition
     * @return the extension entity, if the mob has one
     * @throws IllegalStateException when the extension cannot be spawned or attached
     */
    @Nullable
    public Entity outfit(LivingEntity entity, CustomMob mob) {
        Entity extension = mob.extension().type() != ExtensionType.NONE ? spawnExtension(entity, mob) : null;
        MobTags.setupTags(entity, extension, mob);
        equip(entity, mob.equipment());
        return extension;
    }

    private Entity spawnExtension(LivingEntity base, CustomMob mob) {
        Entity extension =
                base.getWorld().spawnEntity(base.getLocation(), mob.extension().entityType());
        if (!(extension instanceof LivingEntity living)) {
            extension.remove();
            throw new IllegalStateException("Extension of %s is no living entity: %s"
                    .formatted(mob.id(), mob.extension().entityType()));
        }
        if (mob.extension().equipment() != null) equip(living, mob.extension().equipment());
        boolean added =
                switch (mob.extension().type()) {
                    case RIDER -> extension.addPassenger(base);
                    case CARRIER -> base.addPassenger(extension);
                    case NONE -> throw new IllegalStateException("No extension expected");
                };
        if (!added) {
            extension.remove();
            throw new IllegalStateException("Extension of %s could not be attached".formatted(mob.id()));
        }
        return extension;
    }

    private void equip(LivingEntity entity, Equipment equipment) {
        EntityEquipment slots = entity.getEquipment();
        if (slots == null || equipment == null) return;
        slots.setBoots(item(equipment.feet()));
        slots.setLeggings(item(equipment.legs()));
        slots.setChestplate(item(equipment.chest()));
        slots.setHelmet(item(equipment.head()));
        slots.setItemInMainHand(item(equipment.mainHand()));
        slots.setItemInOffHand(item(equipment.offHand()));
    }

    @Nullable
    private ItemStack item(@Nullable String id) {
        if (id == null) return null;
        ItemStack item = items.get().registrations().get(id);
        return item == null ? null : item.clone();
    }
}
