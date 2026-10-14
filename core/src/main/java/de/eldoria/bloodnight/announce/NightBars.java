/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import de.eldoria.bloodnight.configuration.elements.world.announcement.BossBarSettings;
import de.eldoria.bloodnight.night.NightTimes;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One boss bar per player in a world with a running blood night, emptying as the night passes.
 * <p>
 * Every player has a bar of their own, so a bar is only ever changed on its player's thread (on Folia, players in
 * different regions run on different threads).
 */
final class NightBars {
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();

    /**
     * Shows the player their bar with the world's current settings and the night's progress. Runs on the player's
     * thread.
     *
     * @param player   a player in a world with a running blood night
     * @param settings the world's boss bar settings
     * @param times    the world's night times
     * @param night    the running night's number
     */
    void update(Player player, BossBarSettings settings, NightTimes times, long night) {
        if (!settings.enabled()) {
            hide(player);
            return;
        }
        BossBar bar = bars.computeIfAbsent(
                player.getUniqueId(),
                _ -> BossBar.bossBar(Component.empty(), 1f, settings.color(), settings.overlay()));
        bar.name(MiniMessage.miniMessage().deserialize(settings.title()))
                .color(settings.color())
                .overlay(settings.overlay())
                .flags(settings.flags())
                .progress(remaining(times, night, player.getWorld().getFullTime()));
        player.showBossBar(bar);
    }

    /**
     * Hides and forgets the player's bar. Runs on the player's thread.
     */
    void hide(Player player) {
        BossBar bar = bars.remove(player.getUniqueId());
        if (bar != null) player.hideBossBar(bar);
    }

    /**
     * @return the share of the night still to come, 1 at its begin and 0 at its end
     */
    static float remaining(NightTimes times, long night, long fullTime) {
        float share = (float) (times.endOf(night) - fullTime) / times.length();
        return Math.clamp(share, 0f, 1f);
    }
}
