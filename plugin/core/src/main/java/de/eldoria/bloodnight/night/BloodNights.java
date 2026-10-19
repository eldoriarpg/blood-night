/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.api.event.night.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.night.BloodNightEndEvent;
import de.eldoria.bloodnight.api.event.night.EndReason;
import de.eldoria.bloodnight.configuration.world.NightSettings;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * The blood nights running in the worlds. Checked regularly: when a world's night begins and the selector says it is
 * a blood night, the blood night begins, unless a plugin cancels {@link BloodNightBeginEvent}; when the world's time
 * leaves that night, by sunrise or by a jump in time, it ends with {@link BloodNightEndEvent}. A server started in
 * the middle of a blood night picks it up, since the night's answer does not change. Worlds without a day cycle, like
 * the Nether and the End, have timed nights instead (see {@link NightTimes#of(World, NightSettings)}); disabled worlds
 * have no blood nights, and one running when its world is disabled ends. A blood night forced in such a world outside
 * its night has no night to wait for: it begins at once and lasts as long as the world's nights.
 * <p>
 * Everything but {@link #running} runs on the global region, which owns world time and world data; {@link #running} is
 * read from every thread.
 */
public final class BloodNights {
    private final NightSelector selector;
    private final Predicate<World> enabled;
    private final PluginManager events;
    private final Logger logger;
    private final Map<UUID, Long> running = new ConcurrentHashMap<>();
    private final Map<UUID, Long> decided = new ConcurrentHashMap<>();
    private final Set<UUID> held = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> until = new ConcurrentHashMap<>();
    private final Map<UUID, DoubleSupplier> holders = new ConcurrentHashMap<>();
    private final Set<UUID> endsWithHold = ConcurrentHashMap.newKeySet();

    /**
     * @param selector decides the nights
     * @param enabled  whether a world has blood nights at all
     * @param events   where the begin and end events are called
     * @param logger   told when blood nights begin and end
     */
    public BloodNights(NightSelector selector, Predicate<World> enabled, PluginManager events, Logger logger) {
        this.selector = selector;
        this.enabled = enabled;
        this.events = events;
        this.logger = logger;
    }

    /**
     * Begins and ends blood nights as the worlds' times say.
     *
     * @param worlds the worlds to look at
     */
    public void check(Collection<? extends World> worlds) {
        for (World world : worlds) {
            if (!enabled.test(world)) {
                end(world, EndReason.DISABLED);
                continue;
            }
            if (held.contains(world.getUID())) noteHold(world);
            long fullTime = NightTimes.now(world);
            Long deadline = until.get(world.getUID());
            if (deadline != null && (fullTime < deadline || held.contains(world.getUID()))) continue;
            if (deadline != null) end(world, EndReason.TIME);
            Long current = running.get(world.getUID());
            NightTimes times = selector.times(world);
            long night = times.upcoming(fullTime);
            boolean isNight = times.isNight(fullTime);
            if (current != null && (!isNight || current != night) && !held.contains(world.getUID()))
                end(world, EndReason.TIME);
            if (!isNight || running.containsKey(world.getUID())) continue;
            Long seen = decided.put(world.getUID(), night);
            if (seen != null && seen == night) continue;
            selector.forgetBefore(world, night);
            if (selector.isBloodNight(world, night)) begin(world, night);
        }
    }

    /**
     * @param world a world
     * @return whether blood nights are switched on in it
     */
    public boolean enabled(World world) {
        return enabled.test(world);
    }

    /**
     * @param world a world with a running blood night
     * @param times its night times
     * @param night the running night's number
     * @return the time its blood night ends at: for one forced to begin at once in a world without a day cycle, its
     *     own end; otherwise the end of its night
     */
    public long endOf(World world, NightTimes times, long night) {
        Long deadline = until.get(world.getUID());
        return deadline != null ? deadline : times.endOf(night);
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
     * Makes the world's coming night a blood night; when it is night already, the blood night begins now. A world
     * without a day cycle has no night to wait for: outside its night, a blood night begins now, numbered by the
     * period it is in, and lasts as long as its nights.
     *
     * @param world a world
     * @return the forced night's number
     */
    public long force(World world) {
        NightTimes times = selector.times(world);
        long now = NightTimes.now(world);
        if (world.isFixedTime() && !times.isNight(now) && enabled.test(world) && !running.containsKey(world.getUID())) {
            long night = Math.floorDiv(now - times.begin(), times.period());
            until.put(world.getUID(), now + times.length());
            begin(world, night);
            if (!running.containsKey(world.getUID())) until.remove(world.getUID());
            return night;
        }
        long night = times.upcoming(now);
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
        long night = selector.times(world).upcoming(NightTimes.now(world));
        selector.cancel(world, night);
        if (running.containsKey(world.getUID())) end(world, EndReason.CANCELLED);
        return night;
    }

    /**
     * Begins a blood night in the world now, unless one runs, and keeps it running whatever the world's time says until
     * {@link #release}, e.g. for the length of a dragon fight.
     *
     * @param world a world
     * @param left  how much of what holds it is left, from 1 down to 0, e.g. the dragon's share of its health
     */
    public void hold(World world, DoubleSupplier left) {
        if (!enabled.test(world)) return;
        held.add(world.getUID());
        holders.put(world.getUID(), left);
        if (!running.containsKey(world.getUID())) {
            begin(world, selector.times(world).upcoming(NightTimes.now(world)));
        }
        noteHold(world);
    }

    /**
     * @param world a world
     * @return how much of what holds its blood night is left, 1 to 0, while the hold alone keeps the night running;
     *     empty without a hold or when the night outlasts it
     */
    public OptionalDouble holdLeft(World world) {
        DoubleSupplier left = holders.get(world.getUID());
        if (left == null || !endsWithHold.contains(world.getUID())) return OptionalDouble.empty();
        return OptionalDouble.of(Math.clamp(left.getAsDouble(), 0, 1));
    }

    /**
     * Notes whether the world's blood night ends with its hold, so the hold's share can be read from any thread.
     */
    private void noteHold(World world) {
        if (outlastsHold(world)) endsWithHold.remove(world.getUID());
        else endsWithHold.add(world.getUID());
    }

    /**
     * @return whether the running blood night goes on once its hold lets go: it was forced and its time is not up, or
     *     the world's own nights have it running now
     */
    private boolean outlastsHold(World world) {
        Long current = running.get(world.getUID());
        if (current == null) return false;
        long now = NightTimes.now(world);
        Long deadline = until.get(world.getUID());
        if (deadline != null && now < deadline) return true;
        NightTimes times = selector.times(world);
        return times.isNight(now) && times.upcoming(now) == current && selector.isBloodNight(world, current);
    }

    /**
     * @return whether the world's blood night is held, e.g. by a dragon fight
     */
    public boolean held(World world) {
        return held.contains(world.getUID());
    }

    /**
     * Lets a held blood night go: it ends now, unless it was forced and its time is not up, or the world's own nights
     * have a blood night running at this time.
     *
     * @param world a world
     */
    public void release(World world) {
        holders.remove(world.getUID());
        endsWithHold.remove(world.getUID());
        if (!held.remove(world.getUID())) return;
        if (running.containsKey(world.getUID()) && !outlastsHold(world)) end(world, EndReason.RELEASED);
    }

    /**
     * Ends every running blood night, e.g. when the plugin disables.
     */
    public void endAll(Collection<? extends World> worlds) {
        worlds.stream()
                .filter(world -> running.containsKey(world.getUID()))
                .forEach(world -> end(world, EndReason.SHUTDOWN));
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

    private void end(World world, EndReason reason) {
        until.remove(world.getUID());
        Long night = running.remove(world.getUID());
        if (night == null) return;
        events.callEvent(new BloodNightEndEvent(world, night, reason));
        logger.info("Blood night %d ended in %s".formatted(night, world.getName()));
    }
}
