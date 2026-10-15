/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

/**
 * Registry lookups as generated code writes them: {@code Registries.entry(RegistryKey.MOB_EFFECT, "minecraft:speed")}.
 */
public final class Registries {
    private Registries() {}

    /**
     * @param registry the registry
     * @param key      namespaced key of the entry, e.g. {@code minecraft:speed}
     * @param <T>      the registered type
     * @return the entry
     * @throws IllegalArgumentException when the key is malformed
     * @throws java.util.NoSuchElementException when the registry has no such entry
     */
    public static <T extends Keyed> T entry(RegistryKey<T> registry, String key) {
        NamespacedKey namespaced = NamespacedKey.fromString(key);
        if (namespaced == null) throw new IllegalArgumentException("Not a namespaced key: " + key);
        return RegistryAccess.registryAccess().getRegistry(registry).getOrThrow(namespaced);
    }
}
