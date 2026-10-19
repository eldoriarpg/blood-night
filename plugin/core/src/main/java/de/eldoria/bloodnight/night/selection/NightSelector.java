/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night.selection;

import de.eldoria.bloodnight.configuration.world.nightselection.Night;
import de.eldoria.bloodnight.configuration.world.nightselection.NightProbability;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

/**
 * Decides which nights are blood nights, ahead of time.
 * <p>
 * A night is decided by its number alone: it is a blood night when a roll from 0 to 99, drawn from the world's
 * secret salt and the night's number, is below the probability the world's night selection gives that night. So any
 * night can be asked about in advance, the answer does not change when it is asked again, after a restart or a
 * reload, and changed settings apply to the nights to come at once. Admins can force or cancel single nights; those
 * overrides win over the roll.
 * <p>
 * The salt and the overrides live in the world's persistent data container, so they travel with the world. The salt
 * is random and created on first use, so players cannot work the nights out from the world seed.
 */
public final class NightSelector {
    private static final NamespacedKey SALT = new NamespacedKey("bloodnight", "night_salt");
    private static final NamespacedKey FORCED = new NamespacedKey("bloodnight", "forced_nights");
    private static final NamespacedKey CANCELLED = new NamespacedKey("bloodnight", "cancelled_nights");
    private static final long MIX = 0x9E3779B97F4A7C15L;

    private final Function<World, NightProbability> settings;
    private final Function<World, NightTimes> times;
    private final Clock clock;
    private final DoubleSupplier tickRate;

    /**
     * @param settings how likely a world's nights are blood nights: its night selection, or its timed nights when it
     *                 has no day cycle
     * @param times    when a world's nights are
     * @param clock    real time, to place nights for the real moon phase
     * @param tickRate the server's ticks per second
     */
    public NightSelector(
            Function<World, NightProbability> settings,
            Function<World, NightTimes> times,
            Clock clock,
            DoubleSupplier tickRate) {
        this.settings = settings;
        this.times = times;
        this.clock = clock;
        this.tickRate = tickRate;
    }

    /**
     * @param world a world
     * @param night a night's number
     * @return how likely the night is a blood night, 0 to 100, without overrides
     */
    public int probability(World world, long night) {
        return settings.apply(world).probability(new Night(night, expectedStart(world, night)));
    }

    /**
     * @param world a world
     * @param night a night's number
     * @return whether the night is a blood night: forced, or not cancelled and rolled below its probability
     */
    public boolean isBloodNight(World world, long night) {
        if (contains(world, FORCED, night)) return true;
        if (contains(world, CANCELLED, night)) return false;
        return roll(world, night) < probability(world, night);
    }

    /**
     * @param world   a world
     * @param horizon how many nights to look ahead at most
     * @return the next blood night from the one running or about to begin on, if there is one within the horizon
     */
    public Optional<Prediction> next(World world, int horizon) {
        long fullTime = NightTimes.now(world);
        NightTimes nights = times(world);
        long first = nights.upcoming(fullTime);
        for (long night = first; night < first + horizon; night++) {
            if (isBloodNight(world, night)) {
                return Optional.of(
                        new Prediction(night, nights.ticksUntil(fullTime, night), contains(world, FORCED, night)));
            }
        }
        return Optional.empty();
    }

    /**
     * @param world a world
     * @return when its nights are
     */
    public NightTimes times(World world) {
        return times.apply(world);
    }

    /**
     * Makes a night a blood night, whatever the selection says.
     */
    public void force(World world, long night) {
        remove(world, CANCELLED, night);
        add(world, FORCED, night);
    }

    /**
     * Keeps a night an ordinary one, whatever the selection says.
     */
    public void cancel(World world, long night) {
        remove(world, FORCED, night);
        add(world, CANCELLED, night);
    }

    /**
     * Forgets overrides of nights before the given one.
     */
    public void forgetBefore(World world, long night) {
        for (NamespacedKey key : List.of(FORCED, CANCELLED)) {
            List<Long> kept =
                    nights(world, key).stream().filter(n -> n >= night).toList();
            world.getPersistentDataContainer().set(key, PersistentDataType.LIST.longs(), kept);
        }
    }

    /**
     * @return a seed for random choices about a night other than whether it is a blood night, e.g. its length: the
     *     same for the same night, hidden like the roll
     */
    public long seed(World world, long night) {
        return salt(world) ^ ~(night * MIX);
    }

    private int roll(World world, long night) {
        return new SplittableRandom(salt(world) ^ (night * MIX)).nextInt(100);
    }

    private long salt(World world) {
        PersistentDataContainer data = world.getPersistentDataContainer();
        Long salt = data.get(SALT, PersistentDataType.LONG);
        if (salt == null) {
            salt = ThreadLocalRandom.current().nextLong();
            data.set(SALT, PersistentDataType.LONG, salt);
        }
        return salt;
    }

    private Instant expectedStart(World world, long night) {
        long ticks = times(world).ticksUntil(NightTimes.now(world), night);
        double rate = Math.max(tickRate.getAsDouble(), 1);
        return clock.instant().plus(Duration.ofMillis((long) (ticks / rate * 1000)));
    }

    /**
     * @return whether an admin forced the night
     */
    public boolean isForced(World world, long night) {
        return contains(world, FORCED, night);
    }

    /**
     * @return whether an admin cancelled the night
     */
    public boolean isCancelled(World world, long night) {
        return contains(world, CANCELLED, night);
    }

    private boolean contains(World world, NamespacedKey key, long night) {
        return nights(world, key).contains(night);
    }

    private void add(World world, NamespacedKey key, long night) {
        List<Long> nights = new ArrayList<>(nights(world, key));
        if (!nights.contains(night)) nights.add(night);
        world.getPersistentDataContainer().set(key, PersistentDataType.LIST.longs(), nights);
    }

    private void remove(World world, NamespacedKey key, long night) {
        List<Long> nights = new ArrayList<>(nights(world, key));
        nights.remove(night);
        world.getPersistentDataContainer().set(key, PersistentDataType.LIST.longs(), nights);
    }

    private static List<Long> nights(World world, NamespacedKey key) {
        List<Long> nights = world.getPersistentDataContainer().get(key, PersistentDataType.LIST.longs());
        return nights == null ? List.of() : nights;
    }
}
