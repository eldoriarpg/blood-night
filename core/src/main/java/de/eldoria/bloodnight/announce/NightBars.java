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
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * One boss bar per world with a running blood night, shown to the world's players and emptying as the night passes.
 */
final class NightBars {
    private final Map<UUID, BossBar> bars = new HashMap<>();
    private final Supplier<? extends Collection<? extends Player>> online;

    /**
     * @param online the players on the server
     */
    NightBars(Supplier<? extends Collection<? extends Player>> online) {
        this.online = online;
    }

    /**
     * Shows the world's bar to exactly its current players, with the world's current settings.
     *
     * @param world    a world with a running blood night
     * @param settings the world's boss bar settings
     * @param times    the world's night times
     * @param night    the running night's number
     */
    void update(World world, BossBarSettings settings, NightTimes times, long night) {
        if (!settings.enabled()) {
            remove(world);
            return;
        }
        BossBar bar = bars.computeIfAbsent(
                world.getUID(), _ -> BossBar.bossBar(Component.empty(), 1f, settings.color(), settings.overlay()));
        bar.name(MiniMessage.miniMessage().deserialize(settings.title()))
                .color(settings.color())
                .overlay(settings.overlay())
                .flags(settings.flags())
                .progress(remaining(times, night, world.getFullTime()));
        online.get().forEach(player -> {
            if (player.getWorld().equals(world)) player.showBossBar(bar);
            else player.hideBossBar(bar);
        });
    }

    /**
     * Hides the world's bar from a player who left it.
     */
    void hide(World world, Player player) {
        BossBar bar = bars.get(world.getUID());
        if (bar != null) player.hideBossBar(bar);
    }

    /**
     * Hides and forgets the world's bar.
     */
    void remove(World world) {
        BossBar bar = bars.remove(world.getUID());
        if (bar != null) hideAll(bar);
    }

    /**
     * Hides and forgets every bar.
     */
    void clear() {
        bars.values().forEach(this::hideAll);
        bars.clear();
    }

    /**
     * @return the share of the night still to come, 1 at its begin and 0 at its end
     */
    static float remaining(NightTimes times, long night, long fullTime) {
        float share = (float) (times.endOf(night) - fullTime) / times.length();
        return Math.clamp(share, 0f, 1f);
    }

    private void hideAll(BossBar bar) {
        online.get().forEach(player -> player.hideBossBar(bar));
    }
}
