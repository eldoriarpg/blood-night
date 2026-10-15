/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ClockTimeSkipEvent;
import org.bukkit.event.world.TimeSkipEvent;

import java.util.function.Predicate;

/**
 * Keeps players from sleeping through a blood night that is not skippable. Only the skip itself is cancelled: beds
 * stay usable, so players still set their spawn point, and skips by command are left alone.
 */
public final class NightSkipGuard implements Listener {
    private final BloodNights nights;
    private final Predicate<World> skippable;

    /**
     * @param nights    the running blood nights
     * @param skippable whether a world's blood nights may be slept through
     */
    public NightSkipGuard(BloodNights nights, Predicate<World> skippable) {
        this.nights = nights;
        this.skippable = skippable;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSkip(TimeSkipEvent event) {
        if (event.getSkipReason() != ClockTimeSkipEvent.SkipReason.NIGHT_SKIP) return;
        World world = event.getWorld();
        if (nights.running(world).isPresent() && !skippable.test(world)) event.setCancelled(true);
    }
}
