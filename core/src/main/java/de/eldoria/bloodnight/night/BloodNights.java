/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.api.event.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.BloodNightEndEvent;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * The blood nights running in the worlds. Checked regularly: when a world's night begins and the selector says it is
 * a blood night, the blood night begins, unless a plugin cancels {@link BloodNightBeginEvent}; when the world's time
 * leaves that night, by sunrise or by a jump in time, it ends with {@link BloodNightEndEvent}. A server started in
 * the middle of a blood night picks it up, since the night's answer does not change. Worlds with a fixed time, like
 * the nether, have no nights.
 */
public final class BloodNights {
    private final NightSelector selector;
    private final PluginManager events;
    private final Consumer<World> ended;
    private final Logger logger;
    private final Map<UUID, Long> running = new HashMap<>();
    private final Map<UUID, Long> decided = new HashMap<>();

    /**
     * @param selector decides the nights
     * @param events   where the begin and end events are called
     * @param ended    told about every world whose blood night ended, e.g. to run the mobs' night end
     * @param logger   told when blood nights begin and end
     */
    public BloodNights(NightSelector selector, PluginManager events, Consumer<World> ended, Logger logger) {
        this.selector = selector;
        this.events = events;
        this.ended = ended;
        this.logger = logger;
    }

    /**
     * Begins and ends blood nights as the worlds' times say.
     *
     * @param worlds the worlds to look at
     */
    public void check(Collection<? extends World> worlds) {
        for (World world : worlds) {
            if (world.isFixedTime()) continue;
            long fullTime = world.getFullTime();
            Long current = running.get(world.getUID());
            long night = Nights.upcoming(fullTime);
            boolean isNight = Nights.isNight(fullTime);
            if (current != null && (!isNight || current != night)) end(world);
            if (!isNight || running.containsKey(world.getUID())) continue;
            Long seen = decided.put(world.getUID(), night);
            if (seen != null && seen == night) continue;
            selector.forgetBefore(world, night);
            if (selector.isBloodNight(world, night)) begin(world, night);
        }
    }

    /**
     * @param world a world
     * @return the number of the blood night running in it, if one is
     */
    public OptionalLong running(World world) {
        Long night = running.get(world.getUID());
        return night == null ? OptionalLong.empty() : OptionalLong.of(night);
    }

    /**
     * Makes the world's coming night a blood night; when it is night already, the blood night begins now.
     *
     * @param world a world
     * @return the forced night's number
     */
    public long force(World world) {
        long night = Nights.upcoming(world.getFullTime());
        selector.force(world, night);
        decided.remove(world.getUID());
        check(List.of(world));
        return night;
    }

    /**
     * Keeps the world's coming night an ordinary one; a blood night running in it ends now.
     *
     * @param world a world
     * @return the cancelled night's number
     */
    public long cancel(World world) {
        long night = Nights.upcoming(world.getFullTime());
        selector.cancel(world, night);
        if (running.containsKey(world.getUID())) end(world);
        return night;
    }

    /**
     * Ends every running blood night, e.g. when the plugin disables.
     */
    public void endAll(Collection<? extends World> worlds) {
        worlds.stream().filter(world -> running.containsKey(world.getUID())).forEach(this::end);
    }

    private void begin(World world, long night) {
        BloodNightBeginEvent event = new BloodNightBeginEvent(world, night);
        events.callEvent(event);
        if (event.isCancelled()) {
            logger.info("Blood night %d in %s was cancelled by another plugin".formatted(night, world.getName()));
            return;
        }
        running.put(world.getUID(), night);
        logger.info("Blood night %d began in %s".formatted(night, world.getName()));
    }

    private void end(World world) {
        Long night = running.remove(world.getUID());
        if (night == null) return;
        events.callEvent(new BloodNightEndEvent(world, night));
        logger.info("Blood night %d ended in %s".formatted(night, world.getName()));
        ended.accept(world);
    }
}
