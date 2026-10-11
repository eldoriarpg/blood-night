/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.configuration.elements.world.MobSettings;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.AttributeChange;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.ExtensionType;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.bloodnight.util.MobTags;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Turns a spawned entity into a custom mob: extension entity, tags, equipment, name and attributes.
 * <p>
 * The name is MiniMessage, shown above the mob when its world shows names. Health and damage follow
 * {@link Attributes}; further attribute changes are added as modifiers keyed {@code bloodnight:custom_mob_<n>}, so
 * applying a mob twice does not stack them. Values beyond what an attribute allows are clamped by the server.
 */
public final class MobOutfitter {
    private final Supplier<ItemRegistry> items;
    private final Function<World, MobSettings> settings;

    /**
     * @param items    the current item registry
     * @param settings the mob settings of a world
     */
    public MobOutfitter(Supplier<ItemRegistry> items, Function<World, MobSettings> settings) {
        this.items = items;
        this.settings = settings;
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
        MobSettings mobSettings = settings.apply(entity.getWorld());
        name(entity, mob.attributes(), mobSettings.displayNames());
        health(entity, mob.attributes(), mobSettings.healthMultiplier());
        damage(entity, mob.attributes());
        change(entity, mob.attributes().changes());
        return extension;
    }

    private static void name(LivingEntity entity, Attributes attributes, boolean visible) {
        if (attributes.displayName().isBlank()) return;
        entity.customName(MiniMessage.miniMessage().deserialize(attributes.displayName()));
        entity.setCustomNameVisible(visible);
    }

    private static void health(LivingEntity entity, Attributes attributes, double worldMultiplier) {
        AttributeInstance health = entity.getAttribute(Attribute.MAX_HEALTH);
        if (health == null) return;
        health.setBaseValue(
                value(health.getBaseValue(), attributes.healthModifier(), attributes.health(), worldMultiplier));
        entity.setHealth(health.getValue());
    }

    private static void damage(LivingEntity entity, Attributes attributes) {
        AttributeInstance damage = entity.getAttribute(Attribute.ATTACK_DAMAGE);
        if (damage == null) return;
        damage.setBaseValue(value(damage.getBaseValue(), attributes.damageModifier(), attributes.damage(), 1));
    }

    /**
     * @param base            the entity's own value
     * @param modifier        how the configured value applies
     * @param value           the configured value
     * @param defaultMultiplier the factor for {@link ValueModifier#DEFAULT}
     * @return the new base value, at least 0
     */
    static double value(double base, ValueModifier modifier, double value, double defaultMultiplier) {
        double result =
                switch (modifier) {
                    case DEFAULT -> base * defaultMultiplier;
                    case MULTIPLY -> base * value;
                    case VALUE -> value;
                };
        return Math.max(result, 0);
    }

    private static void change(LivingEntity entity, List<AttributeChange> changes) {
        var registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ATTRIBUTE);
        for (int i = 0; i < changes.size(); i++) {
            AttributeChange change = changes.get(i);
            NamespacedKey key = NamespacedKey.fromString(change.attribute());
            Attribute attribute = key == null ? null : registry.get(key);
            AttributeInstance instance = attribute == null ? null : entity.getAttribute(attribute);
            if (instance == null) continue;
            NamespacedKey modifierKey = new NamespacedKey("bloodnight", "custom_mob_" + i);
            instance.removeModifier(modifierKey);
            instance.addModifier(
                    new AttributeModifier(modifierKey, change.amount(), change.operation(), EquipmentSlotGroup.ANY));
        }
    }

    private Entity spawnExtension(LivingEntity base, CustomMob mob) {
        EntityType type = mob.extension().entityType();
        if (type == null) throw new IllegalStateException("Extension of %s has no entity type".formatted(mob.id()));
        Entity extension = base.getWorld().spawnEntity(base.getLocation(), type);
        if (!(extension instanceof LivingEntity living)) {
            extension.remove();
            throw new IllegalStateException("Extension of %s is no living entity: %s".formatted(mob.id(), type));
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

    private void equip(LivingEntity entity, @Nullable Equipment equipment) {
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
