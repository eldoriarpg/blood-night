/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import de.eldoria.bloodnight.api.event.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.BloodNightEndEvent;
import de.eldoria.bloodnight.configuration.elements.world.AtmosphereSettings;
import de.eldoria.bloodnight.night.BloodNights;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Turns the sky of a world red during its blood nights.
 * <p>
 * The plugin's data pack adds the world clock {@value #CLOCK} and a timeline on it to the overworld, the Nether and
 * the End.
 * The timeline blends sky, fog, clouds and light to red between clock time 0 and {@value #FULL}, holds them until
 * {@value #FADE_OUT}, and blends them back by {@value #REST}; outside of that it changes nothing. Clients play the
 * timeline themselves, so a blood night only needs the clock set at its begin and end. Paper keeps a clock per world
 * and clocks run on their own; the clock is set with {@code /time of} in the world, on the global region, by a sender
 * that keeps the command's feedback out of the console.
 */
public final class BloodSky implements Listener {
    static final String CLOCK = "bloodnight:blood_night";
    static final long FULL = 200;
    static final long FADE_OUT = 1_000_000;
    static final long REST = 1_000_200;
    private final BloodNights nights;
    private final Function<World, AtmosphereSettings> settings;
    private final Consumer<String> commands;
    private final Set<UUID> red = ConcurrentHashMap.newKeySet();

    /**
     * @param nights   the running blood nights
     * @param settings the atmosphere settings of a world
     * @param commands runs a command on the global region without showing its feedback, see {@link #silently}
     */
    public BloodSky(BloodNights nights, Function<World, AtmosphereSettings> settings, Consumer<String> commands) {
        this.nights = nights;
        this.settings = settings;
        this.commands = commands;
    }

    /**
     * @return runs commands as the console would, with their feedback dropped
     */
    public static Consumer<String> silently(Server server) {
        CommandSender silent = server.createCommandSender(feedback -> {});
        return command -> server.dispatchCommand(silent, command);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBegin(BloodNightBeginEvent event) {
        World world = event.getWorld();
        if (!settings.apply(world).sky()) return;
        red.add(world.getUID());
        clock(world, 0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnd(BloodNightEndEvent event) {
        World world = event.getWorld();
        if (red.remove(world.getUID())) clock(world, FADE_OUT);
    }

    /**
     * Sets every world's clock to what its nights say: red at once for a blood night that already runs, e.g. after a
     * restart, unchanged otherwise. Runs on the global region when the plugin enables.
     */
    public void restore(Collection<? extends World> worlds) {
        for (World world : worlds) {
            boolean bloodNight =
                    nights.running(world).isPresent() && settings.apply(world).sky();
            if (bloodNight) red.add(world.getUID());
            clock(world, bloodNight ? FULL : REST);
            command(world, "resume");
        }
    }

    /**
     * Gives every red world its ordinary sky back at once, e.g. when the plugin disables.
     */
    public void clear(Collection<? extends World> worlds) {
        for (World world : worlds) {
            if (red.remove(world.getUID())) clock(world, REST);
        }
    }

    private void clock(World world, long ticks) {
        command(world, "set " + ticks);
    }

    private void command(World world, String change) {
        commands.accept(
                "execute in %s run time of %s %s".formatted(world.getKey().asString(), CLOCK, change));
    }
}
