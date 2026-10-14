/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.graph.runtime.Integrations;
import de.eldoria.bloodnight.mob.MobTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * What the server offers to pick from, for the editor's item fields: the materials that are items and the
 * enchantments, data pack ones included, by key, and the Minecraft version, whose icons and names the editor shows.
 *
 * @param minecraftVersion the server's Minecraft version, e.g. {@code 26.3}
 * @param materials        material keys, sorted
 * @param enchantments     enchantment keys, sorted
 * @param entityClasses    the Bukkit interface of each entity type, by type name, e.g. {@code CREEPER} to
 *                         {@code org.bukkit.entity.Creeper}; the editor types Self and Extension of a mob by it
 * @param mobTypes         the entity types a custom mob or its extension can be, by name, sorted; see {@link MobTypes}
 * @param mythicTypes      the MythicMobs mobs a custom mob can spawn as, sorted; empty without MythicMobs
 * @param attributes       attribute keys a mob's attributes can change, sorted
 * @param biomes           biome keys and biome tags ({@code #minecraft:is_nether}) spawn rules can name, sorted
 */
public record EditorChoices(
        String minecraftVersion,
        List<String> materials,
        List<String> enchantments,
        Map<String, String> entityClasses,
        List<String> mobTypes,
        List<String> mythicTypes,
        List<String> attributes,
        List<String> biomes) {
    /**
     * @return the choices of the running server
     */
    public static EditorChoices ofServer() {
        List<String> materials = Arrays.stream(Material.values())
                .filter(material -> !material.isLegacy() && material.isItem() && !material.isAir())
                .map(material -> material.getKey().asString())
                .sorted()
                .toList();
        List<String> enchantments = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream()
                .map(enchantment -> enchantment.getKey().asString())
                .sorted()
                .toList();
        Map<String, String> entityClasses = new TreeMap<>();
        for (EntityType type : EntityType.values()) {
            Class<?> entityClass = type.getEntityClass();
            if (entityClass != null) entityClasses.put(type.name(), entityClass.getName());
        }
        return new EditorChoices(
                Bukkit.getMinecraftVersion(),
                materials,
                enchantments,
                entityClasses,
                MobTypes.all(),
                Integrations.mythicMobTypes(),
                keys(RegistryKey.ATTRIBUTE),
                biomeKeys());
    }

    private static List<String> keys(RegistryKey<? extends Keyed> key) {
        return RegistryAccess.registryAccess().getRegistry(key).stream()
                .map(entry -> entry.getKey().asString())
                .sorted()
                .toList();
    }

    private static List<String> biomeKeys() {
        Registry<Biome> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        Stream<String> tags =
                registry.getTags().stream().map(tag -> "#" + tag.tagKey().key().asString());
        return Stream.concat(keys(RegistryKey.BIOME).stream(), tags).sorted().toList();
    }
}
