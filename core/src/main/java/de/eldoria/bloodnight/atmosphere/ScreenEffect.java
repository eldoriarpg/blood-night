/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The resource pack's red screen effect, {@value #ID}, switched on and off for one player.
 * <p>
 * Screen effects (post effects) arrived with Minecraft 26.3; the plugin is built against 26.2, so the 26.3 methods are
 * looked up at runtime and nothing happens on older servers. A player without the pack sees nothing either.
 */
final class ScreenEffect {
    static final String ID = "bloodnight:blood";
    private static final Key KEY = Key.key(ID);
    private static final @Nullable Method POST_EFFECTS = method(Player.class, "postEffects");

    private ScreenEffect() {}

    /**
     * @return whether the server can show screen effects
     */
    static boolean available() {
        return POST_EFFECTS != null;
    }

    static void show(Player player) {
        call(player, "add");
    }

    static void hide(Player player) {
        call(player, "remove");
    }

    private static void call(Player player, String change) {
        if (POST_EFFECTS == null) return;
        try {
            Object effects = POST_EFFECTS.invoke(player);
            effects.getClass().getMethod(change, Key.class).invoke(effects, KEY);
            effects.getClass().getMethod("update").invoke(effects);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Screen effects changed their API",
                    e instanceof InvocationTargetException ite ? ite.getCause() : e);
        }
    }

    private static @Nullable Method method(Class<?> type, String name) {
        try {
            return type.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
