/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import de.eldoria.bloodnight.api.event.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.BloodNightEndEvent;
import de.eldoria.bloodnight.night.BloodNights;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Moves the world clocks of the plugin's data pack timelines with a world's blood nights, for the
 * {@link TimelineGroup}s the world has switched on.
 * <p>
 * The data pack adds a world clock and a timeline per group to the overworld, the Nether and the End. Clients play the
 * visual timelines themselves and the server samples the gameplay ones, so a blood night only needs the clocks set at
 * its begin and end. Paper keeps a clock per world and clocks run on their own; a clock is set with
 * {@code /minecraft:time of} (plugins such as EssentialsX replace {@code /time}) in the world, on the global region, by
 * a sender that keeps the command's feedback out of the console. Groups a world
 * has off stay at rest.
 */
public final class Timelines implements Listener {
    private final BloodNights nights;
    private final Function<World, Set<TimelineGroup>> groups;
    private final Consumer<String> commands;
    private final Map<UUID, Set<TimelineGroup>> started = new ConcurrentHashMap<>();

    /**
     * @param nights   the running blood nights
     * @param groups   the groups a world has switched on
     * @param commands runs a command on the global region without showing its feedback, see {@link #silently}
     */
    public Timelines(BloodNights nights, Function<World, Set<TimelineGroup>> groups, Consumer<String> commands) {
        this.nights = nights;
        this.groups = groups;
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
        Set<TimelineGroup> lit = started(world);
        for (TimelineGroup group : groups.apply(world)) {
            if (group.dawn()) continue;
            lit.add(group);
            clock(world, group, 0);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnd(BloodNightEndEvent event) {
        World world = event.getWorld();
        Set<TimelineGroup> lit = started(world);
        for (TimelineGroup group : Set.copyOf(lit)) {
            if (group.dawn()) continue;
            lit.remove(group);
            clock(world, group, TimelineGroup.FADE_OUT);
        }
        for (TimelineGroup group : groups.apply(world)) {
            if (!group.dawn()) continue;
            lit.add(group);
            clock(world, group, 0);
        }
    }

    /**
     * Sets every world's clocks to what its nights say: in full effect at once for a blood night that already runs,
     * e.g. after a restart, at rest otherwise. Runs on the global region when the plugin enables.
     */
    public void restore(Collection<? extends World> worlds) {
        for (World world : worlds) {
            Set<TimelineGroup> on = nights.running(world).isPresent() ? groups.apply(world) : Set.of();
            for (TimelineGroup group : TimelineGroup.values()) {
                boolean full = on.contains(group) && !group.dawn();
                if (full) started(world).add(group);
                clock(world, group, full ? TimelineGroup.FULL : group.rest());
                command(world, group, "resume");
            }
        }
    }

    /**
     * Puts every started group to rest at once, e.g. when the plugin disables. Commands only run on the global region;
     * elsewhere (Folia stops plugins off it) nothing happens, and {@link #restore} sets the clocks on the next start
     * instead.
     *
     * @param global whether the current thread is the global region's
     */
    public void clear(Collection<? extends World> worlds, boolean global) {
        if (!global) return;
        for (World world : worlds) {
            Set<TimelineGroup> lit = started.remove(world.getUID());
            if (lit == null) continue;
            for (TimelineGroup group : lit) clock(world, group, group.rest());
        }
    }

    private Set<TimelineGroup> started(World world) {
        return started.computeIfAbsent(
                world.getUID(), ignored -> Collections.synchronizedSet(EnumSet.noneOf(TimelineGroup.class)));
    }

    private void clock(World world, TimelineGroup group, long ticks) {
        command(world, group, "set " + ticks);
    }

    private void command(World world, TimelineGroup group, String change) {
        commands.accept("execute in %s run minecraft:time of %s %s"
                .formatted(world.getKey().asString(), group.key(), change));
    }
}
