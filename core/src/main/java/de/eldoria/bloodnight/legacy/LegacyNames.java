/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.legacy;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Turns what the old plugin stored by Bukkit's old names into today's: potion effects by their old enum names
 * ({@code CONFUSION} is {@code minecraft:nausea}), sounds by their enum names ({@code ENTITY_LIGHTNING_BOLT_THUNDER} is
 * {@code minecraft:entity.lightning_bolt.thunder}), and items from Bukkit's own item maps.
 */
final class LegacyNames {
    private static final Map<String, String> RENAMED_EFFECTS = Map.of(
            "CONFUSION", "nausea",
            "SLOW", "slowness",
            "FAST_DIGGING", "haste",
            "SLOW_DIGGING", "mining_fatigue",
            "INCREASE_DAMAGE", "strength",
            "HEAL", "instant_health",
            "HARM", "instant_damage",
            "JUMP", "jump_boost",
            "DAMAGE_RESISTANCE", "resistance");

    private LegacyNames() {}

    /**
     * @param name an old effect name, e.g. {@code CONFUSION}
     * @return its key, e.g. {@code minecraft:nausea}
     */
    static String effect(String name) {
        return "minecraft:" + RENAMED_EFFECTS.getOrDefault(name, name.toLowerCase(Locale.ROOT));
    }

    /**
     * Finds the sound whose key, flattened the way the old enum names were ({@code .} to {@code _}, upper case), is
     * the old name.
     *
     * @param name an old sound name, e.g. {@code AMBIENT_BASALT_DELTAS_ADDITIONS}
     * @return its key, e.g. {@code minecraft:ambient.basalt_deltas.additions}; empty when no sound has that name
     */
    static Optional<String> sound(String name) {
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.SOUND_EVENT)
                .keyStream()
                .filter(key ->
                        key.getKey().replace('.', '_').toUpperCase(Locale.ROOT).equals(name))
                .map(NamespacedKey::asString)
                .findFirst();
    }

    /**
     * Builds an item from Bukkit's map of it, as the old plugin's YAML holds it, with its {@code ==} type tags.
     *
     * @return the item; empty when the server cannot read it any more
     */
    static Optional<ItemStack> item(@Nullable Object stored) {
        try {
            return deserialize(stored) instanceof ItemStack item ? Optional.of(item) : Optional.empty();
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static @Nullable Object deserialize(@Nullable Object value) {
        if (value instanceof List<?> list) {
            List<Object> out = new ArrayList<>();
            for (Object element : list) out.add(deserialize(element));
            return out;
        }
        if (!(value instanceof Map<?, ?> map)) return value;
        Map<String, Object> out = new LinkedHashMap<>();
        map.forEach((key, element) -> out.put(String.valueOf(key), deserialize(element)));
        if (!out.containsKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) return out;
        return ConfigurationSerialization.deserializeObject(out);
    }
}
