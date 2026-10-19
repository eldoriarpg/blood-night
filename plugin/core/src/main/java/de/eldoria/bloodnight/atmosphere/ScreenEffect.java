/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import io.papermc.paper.entity.PlayerPostEffects;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;

/**
 * The resource pack's red screen effect, {@value #ID}, switched on and off for one player. A player without the pack
 * sees nothing.
 */
final class ScreenEffect {
    static final String ID = "bloodnight:blood";
    private static final Key KEY = Key.key(ID);

    private ScreenEffect() {}

    static void show(Player player) {
        PlayerPostEffects effects = player.postEffects();
        if (effects.add(KEY)) effects.update();
    }

    static void hide(Player player) {
        PlayerPostEffects effects = player.postEffects();
        if (effects.remove(KEY)) effects.update();
    }
}
