/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.types.TypeMapping;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.AttributeKeys;
import io.papermc.paper.registry.keys.BiomeKeys;
import io.papermc.paper.registry.keys.BlockTypeKeys;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.ItemTypeKeys;
import io.papermc.paper.registry.keys.MobEffectKeys;
import io.papermc.paper.registry.keys.SoundEventKeys;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * Bukkit types as the editor knows them: names, literal codecs and the types users can pick for macro inputs.
 * <p>
 * Every type with a Paper registry gets a {@link RegistryCodec}, enums included; {@code Material} is deliberately
 * absent, items and blocks use {@link ItemType} and {@link BlockType}.
 */
public final class BukkitTypes {
    private BukkitTypes() {}

    /**
     * Types reflection may turn into ports, besides primitives, strings, enums and collections of accepted types.
     */
    static final List<Class<?>> KNOWN = List.of(
            Entity.class,
            Location.class,
            Vector.class,
            World.class,
            ItemStack.class,
            Block.class,
            UUID.class,
            PotionEffectType.class,
            PotionEffect.class,
            EntityType.class,
            ItemType.class,
            BlockType.class,
            Particle.class,
            Sound.class,
            Enchantment.class,
            Attribute.class,
            Biome.class);

    /**
     * @return the type mapping of the blood-night platform
     */
    public static TypeMapping mapping() {
        return TypeMapping.builder()
                .name(Entity.class, "Entity")
                .name(LivingEntity.class, "Living Entity")
                .name(Player.class, "Player")
                .name(Projectile.class, "Projectile")
                .name(Location.class, "Location")
                .name(Vector.class, "Vector")
                .name(World.class, "World")
                .name(ItemStack.class, "Item")
                .name(Block.class, "Block")
                .name(EntityDamageEvent.DamageCause.class, "Damage Cause")
                .name(PotionEffectType.class, "Potion Effect Type")
                .name(PotionEffect.class, "Potion Effect")
                .name(EntityType.class, "Entity Type")
                .name(ItemType.class, "Item Type")
                .name(BlockType.class, "Block Type")
                .codec(registry(EntityType.class, RegistryKey.ENTITY_TYPE, "ENTITY_TYPE", null))
                .codec(registry(PotionEffectType.class, RegistryKey.MOB_EFFECT, "MOB_EFFECT", MobEffectKeys.class))
                .codec(registry(ItemType.class, RegistryKey.ITEM, "ITEM", ItemTypeKeys.class))
                .codec(registry(BlockType.class, RegistryKey.BLOCK, "BLOCK", BlockTypeKeys.class))
                .codec(registry(Particle.class, RegistryKey.PARTICLE_TYPE, "PARTICLE_TYPE", null))
                .codec(registry(Sound.class, RegistryKey.SOUND_EVENT, "SOUND_EVENT", SoundEventKeys.class))
                .codec(registry(Enchantment.class, RegistryKey.ENCHANTMENT, "ENCHANTMENT", EnchantmentKeys.class))
                .codec(registry(Attribute.class, RegistryKey.ATTRIBUTE, "ATTRIBUTE", AttributeKeys.class))
                .codec(registry(Biome.class, RegistryKey.BIOME, "BIOME", BiomeKeys.class))
                .selectable("entity", PortType.of(Entity.class))
                .selectable("living_entity", PortType.of(LivingEntity.class))
                .selectable("player", PortType.of(Player.class))
                .selectable("location", PortType.of(Location.class))
                .selectable("vector", PortType.of(Vector.class))
                .selectable("world", PortType.of(World.class))
                .selectable("item", PortType.of(ItemStack.class))
                .selectable("damage_cause", PortType.of(EntityDamageEvent.DamageCause.class))
                .selectable("entity_type", PortType.of(EntityType.class))
                .selectable("potion_effect", PortType.of(PotionEffectType.class))
                .build();
    }

    private static <T extends Keyed> RegistryCodec<T> registry(
            Class<T> type, RegistryKey<T> key, String field, @Nullable Class<?> keys) {
        return new RegistryCodec<>(type, key, field, keys);
    }
}
