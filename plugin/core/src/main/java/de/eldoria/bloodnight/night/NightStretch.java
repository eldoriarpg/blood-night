/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.api.event.night.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.night.BloodNightEndEvent;
import de.eldoria.bloodnight.configuration.world.NightSettings;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToLongBiFunction;

/**
 * Gives blood nights their configured length by stretching (or shrinking) the night while the day cycle keeps
 * running.
 * <p>
 * Every tick the world's time is set to exactly where the night stands after the ticks that have passed; setting it
 * every tick, not now and then, keeps the sun and moon from jumping. A blood night that begins late, e.g. forced in
 * the middle of the night or picked up after a restart, gets the share of its length that is left. When the time was
 * moved from outside (by more than the one tick the world advances by itself), the stretch lets go and does not fight
 * it. Worlds without a day cycle have timed nights of their own length and are never stretched.
 */
public final class NightStretch implements Listener {
    private final Server server;
    private final Function<World, NightSettings> settings;
    private final Function<World, NightTimes> times;
    private final ToLongBiFunction<World, Long> seeds;
    private final Map<UUID, Stretch> active = new HashMap<>();

    /**
     * @param server   finds the worlds
     * @param settings a world's night settings
     * @param times    when a world's nights are
     * @param seeds    the seed of a world's night, see {@link NightSelector#seed(World, long)}
     */
    public NightStretch(
            Server server,
            Function<World, NightSettings> settings,
            Function<World, NightTimes> times,
            ToLongBiFunction<World, Long> seeds) {
        this.server = server;
        this.settings = settings;
        this.times = times;
        this.seeds = seeds;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBegin(BloodNightBeginEvent event) {
        World world = event.getWorld();
        if (world.isFixedTime()) return;
        OptionalLong length = settings.apply(world).length().ticks(seeds.applyAsLong(world, event.night()));
        if (length.isEmpty()) return;
        NightTimes nights = times.apply(world);
        long from = world.getFullTime();
        long to = nights.endOf(event.night());
        if (to <= from || nights.length() == 0) return;
        long ticks = Math.max(1, length.getAsLong() * (to - from) / nights.length());
        active.put(world.getUID(), new Stretch(from, to, ticks));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnd(BloodNightEndEvent event) {
        active.remove(event.getWorld().getUID());
    }

    /**
     * Moves every stretched night on by one tick.
     */
    public void tick() {
        active.entrySet().removeIf(entry -> {
            World world = server.getWorld(entry.getKey());
            return world == null || !entry.getValue().advance(world);
        });
    }

    /**
     * Lets go of every night, e.g. when the plugin disables.
     */
    public void clear() {
        active.clear();
    }

    /**
     * One stretched night: from {@code from} to {@code to} in world time, over {@code ticks} ticks.
     */
    private static final class Stretch {
        private final long from;
        private final long to;
        private final long ticks;
        private long elapsed;
        private long set = -1;

        private Stretch(long from, long to, long ticks) {
            this.from = from;
            this.to = to;
            this.ticks = ticks;
        }

        /**
         * @return whether the night is still being stretched
         */
        boolean advance(World world) {
            if (set >= 0 && Math.abs(world.getFullTime() - set) > 1) return false;
            elapsed++;
            set = from + (to - from) * Math.min(elapsed, ticks) / ticks;
            world.setFullTime(set);
            return elapsed < ticks;
        }
    }
}
