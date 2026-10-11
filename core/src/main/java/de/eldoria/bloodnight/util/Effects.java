/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.Nullable;

/**
 * Looks up potion effects named in settings.
 */
public final class Effects {
    private Effects() {}

    /**
     * @param key an effect's key, e.g. {@code minecraft:darkness}
     * @return the effect, or null when the key is malformed or names no effect
     */
    public static @Nullable PotionEffectType effect(String key) {
        NamespacedKey namespaced = NamespacedKey.fromString(key);
        if (namespaced == null) return null;
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.MOB_EFFECT)
                .get(namespaced);
    }
}
