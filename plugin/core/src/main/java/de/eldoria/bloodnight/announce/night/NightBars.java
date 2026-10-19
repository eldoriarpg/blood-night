/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce.night;

import de.eldoria.bloodnight.configuration.world.announcement.BossBarSettings;
import de.eldoria.bloodnight.night.selection.NightTimes;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One boss bar per player in a world with a running blood night, emptying as the night passes.
 * <p>
 * Every player has a bar of their own, so a bar is only ever changed on its player's thread (on Folia, players in
 * different regions run on different threads). A darkened player's bar also darkens their sky and world, which the
 * client fades in and out by itself.
 */
public final class NightBars {
    private final Map<UUID, BossBar> bars = new ConcurrentHashMap<>();
    private final Set<UUID> darkened = ConcurrentHashMap.newKeySet();

    /**
     * Shows the player their bar with the world's current settings and the night's progress. Runs on the player's
     * thread.
     *
     * @param player   a player in a world with a running blood night
     * @param settings the world's boss bar settings
     * @param times    the world's night times
     * @param end      the time the running night ends at
     * @param name     the key of the night's name, filled into the title's {@code <name>}
     */
    public void update(Player player, BossBarSettings settings, NightTimes times, long end, String name) {
        if (!settings.enabled()) {
            hide(player);
            return;
        }
        BossBar bar = bars.computeIfAbsent(
                player.getUniqueId(),
                _ -> BossBar.bossBar(Component.empty(), 1f, settings.color(), settings.overlay()));
        bar.name(MiniMessage.miniMessage()
                        .deserialize(settings.title(), Placeholder.component("name", Component.translatable(name))))
                .color(settings.color())
                .overlay(settings.overlay())
                .flags(flags(player, settings))
                .progress(remaining(times, end, NightTimes.now(player.getWorld())));
        player.showBossBar(bar);
    }

    /**
     * Hides and forgets the player's bar. Runs on the player's thread.
     */
    public void hide(Player player) {
        darkened.remove(player.getUniqueId());
        BossBar bar = bars.remove(player.getUniqueId());
        if (bar != null) player.hideBossBar(bar);
    }

    /**
     * Darkens the player's world through their bar, or lets it brighten again; nothing happens without a bar. Runs on
     * the player's thread.
     */
    public void darken(Player player, boolean dark) {
        if (dark) darkened.add(player.getUniqueId());
        else darkened.remove(player.getUniqueId());
        BossBar bar = bars.get(player.getUniqueId());
        if (bar == null) return;
        if (dark) bar.addFlag(BossBar.Flag.DARKEN_SCREEN);
        else bar.removeFlag(BossBar.Flag.DARKEN_SCREEN);
    }

    private Set<BossBar.Flag> flags(Player player, BossBarSettings settings) {
        Set<BossBar.Flag> flags = EnumSet.noneOf(BossBar.Flag.class);
        flags.addAll(settings.flags());
        if (darkened.contains(player.getUniqueId())) flags.add(BossBar.Flag.DARKEN_SCREEN);
        return flags;
    }

    /**
     * @param end the time the night ends at
     * @return the share of the night still to come, 1 at its begin and 0 at its end
     */
    static float remaining(NightTimes times, long end, long fullTime) {
        float share = (float) (end - fullTime) / times.length();
        return Math.clamp(share, 0f, 1f);
    }
}
