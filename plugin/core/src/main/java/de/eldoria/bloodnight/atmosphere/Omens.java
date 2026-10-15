/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import de.eldoria.bloodnight.configuration.elements.world.AtmosphereSettings;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.random.RandomGenerator;

/**
 * Foreshadows a blood night in the last {@value TimelineGroup#OMEN_TICKS} ticks before it begins, as the world's atmosphere
 * settings say: the {@link TimelineGroup#OMEN} timelines turn the sunset deep red and let the first ash fall, players
 * hear distant bells and wolves, and animals near players bolt away from them.
 * <p>
 * {@link #tick()} runs every {@value #STEP} ticks on the global region: it starts the omen timelines once per coming
 * night, also late, e.g. after a restart, and hands each player of the world to their own thread.
 */
public final class Omens {
    /**
     * Ticks between two steps of {@link #tick()}.
     */
    public static final int STEP = 20;

    static final double SOUND_CHANCE = 1.0 / 15;
    static final double ANIMAL_CHANCE = 1.0 / 8;
    private static final double SOUND_DISTANCE = 40;
    private static final double ANIMAL_REACH = 16;
    private static final double BOLT_DISTANCE = 10;
    private static final double BOLT_SPEED = 1.6;
    private static final List<Sound> SOUNDS = List.of(
            Sound.sound(Key.key("block.bell.use"), Sound.Source.AMBIENT, 0.8f, 0.5f),
            Sound.sound(Key.key("block.bell.resonate"), Sound.Source.AMBIENT, 0.6f, 0.6f),
            Sound.sound(Key.key("entity.wolf.whine"), Sound.Source.AMBIENT, 1f, 0.5f));

    private final Server server;
    private final ToLongFunction<World> ticksUntil;
    private final Function<World, AtmosphereSettings> settings;
    private final Timelines timelines;
    private final BiConsumer<Entity, Runnable> onThread;
    private final RandomGenerator random;
    private final BiConsumer<Mob, Location> run;
    private final Map<UUID, Long> foreshadowed = new ConcurrentHashMap<>();

    /**
     * @param server     the server
     * @param ticksUntil ticks until the world's next blood night begins, or a negative number when none is coming
     *                   within a night
     * @param settings   the atmosphere settings of a world
     * @param timelines  starts the omen timelines
     * @param onThread   runs work on an entity's thread, right away when the current thread owns it
     * @param random     the source of chance
     * @param run        sends a mob running to a location, e.g. {@link #runTo}
     */
    public Omens(
            Server server,
            ToLongFunction<World> ticksUntil,
            Function<World, AtmosphereSettings> settings,
            Timelines timelines,
            BiConsumer<Entity, Runnable> onThread,
            RandomGenerator random,
            BiConsumer<Mob, Location> run) {
        this.server = server;
        this.ticksUntil = ticksUntil;
        this.settings = settings;
        this.timelines = timelines;
        this.onThread = onThread;
        this.random = random;
        this.run = run;
    }

    /**
     * Sends a mob running along a path to a location, faster than it walks.
     */
    public static void runTo(Mob mob, Location location) {
        mob.getPathfinder().moveTo(location, BOLT_SPEED);
    }

    /**
     * Foreshadows the blood nights about to begin.
     */
    public void tick() {
        for (World world : server.getWorlds()) {
            long until = ticksUntil.applyAsLong(world);
            if (until <= 0 || until > TimelineGroup.OMEN_TICKS) continue;
            long begins = world.getFullTime() + until;
            Long known = foreshadowed.put(world.getUID(), begins);
            if (known == null || Math.abs(known - begins) > STEP)
                timelines.omen(world, TimelineGroup.OMEN_TICKS - until);
            AtmosphereSettings atmosphere = settings.apply(world);
            if (!atmosphere.omenSounds() && !atmosphere.restlessAnimals()) continue;
            for (Player player : world.getPlayers()) onThread.accept(player, () -> unsettle(player, atmosphere));
        }
    }

    private void unsettle(Player player, AtmosphereSettings atmosphere) {
        if (atmosphere.omenSounds() && random.nextDouble() < SOUND_CHANCE) sound(player);
        if (atmosphere.restlessAnimals() && random.nextDouble() < ANIMAL_CHANCE) bolt(player);
    }

    private void sound(Player player) {
        double angle = random.nextDouble(2 * Math.PI);
        Location from = player.getLocation().add(Math.cos(angle) * SOUND_DISTANCE, 0, Math.sin(angle) * SOUND_DISTANCE);
        player.playSound(SOUNDS.get(random.nextInt(SOUNDS.size())), from.getX(), from.getY(), from.getZ());
    }

    /**
     * Sends animals near the player running away from them; tamed ones stay calm.
     */
    void bolt(Player player) {
        Location at = player.getLocation();
        for (Entity entity : player.getNearbyEntities(ANIMAL_REACH, ANIMAL_REACH / 2, ANIMAL_REACH)) {
            if (!(entity instanceof Animals animal)) continue;
            if (animal instanceof Tameable tameable && tameable.isTamed()) continue;
            Vector away =
                    animal.getLocation().toVector().subtract(at.toVector()).setY(0);
            if (away.lengthSquared() < 1e-4) away = new Vector(1, 0, 0);
            Location target = animal.getLocation().add(away.normalize().multiply(BOLT_DISTANCE));
            run.accept(animal, target);
        }
    }
}
