/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.tasks;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;

/**
 * Runs work on the thread that owns what it touches, on Paper and on Folia alike.
 * <p>
 * Folia ticks every region of a world on its own thread and has no main thread: an entity is only touched by the
 * thread of its region, world-wide state such as the time and console commands by the global region. Paper offers
 * the same schedulers and runs all of them on its main thread, so one code path serves both.
 */
public final class Tasks {
    private static final boolean REGIONISED = classExists("io.papermc.paper.threadedregions.RegionizedServer");
    private final Plugin plugin;
    private final Server server;
    private @Nullable ExecutorService sender;

    /**
     * @param plugin the plugin owning the tasks
     */
    public Tasks(Plugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    /**
     * @return whether the server ticks regions on their own threads, i.e. runs Folia
     */
    public static boolean regionised() {
        return REGIONISED;
    }

    /**
     * Runs work on the global region on its next tick: world time, world data, console commands.
     */
    public void global(Runnable work) {
        server.getGlobalRegionScheduler().execute(plugin, work);
    }

    /**
     * Runs work on the global region: right away when the current thread is the global one, otherwise on its next
     * tick.
     */
    public void globalNow(Runnable work) {
        if (server.isGlobalTickThread()) work.run();
        else global(work);
    }

    /**
     * Runs work on the global region every {@code period} ticks, starting after one period.
     */
    public ScheduledTask globalTimer(long period, Runnable work) {
        return server.getGlobalRegionScheduler().runAtFixedRate(plugin, task -> work.run(), period, period);
    }

    /**
     * Runs work on the entity's thread on its next tick; nothing runs when the entity is gone by then.
     */
    public void entity(Entity entity, Runnable work) {
        entity.getScheduler().execute(plugin, work, null, 1);
    }

    /**
     * @return the plugin the tasks run for
     */
    public Plugin plugin() {
        return plugin;
    }

    /**
     * Runs work on the entity's thread after a delay; when the entity is gone by then, {@code retired} runs instead.
     *
     * @param ticks   the delay, at least one tick
     * @param retired runs when the entity is gone before the work could run
     */
    public void entityLater(Entity entity, long ticks, Runnable work, Runnable retired) {
        entity.getScheduler().execute(plugin, work, retired, Math.max(ticks, 1));
    }

    /**
     * Runs work on the entity's thread right away when the current thread owns it, otherwise on its next tick.
     */
    public void entityNow(Entity entity, Runnable work) {
        if (server.isOwnedByCurrentRegion(entity)) work.run();
        else entity(entity, work);
    }

    /**
     * Runs work on the entity's thread every tick, starting with the next one, for as long as it returns true and the
     * entity exists. Each run schedules the next one, so stopping needs no handle to cancel.
     *
     * @param work    one step; false stops the loop
     * @param retired runs when the entity is gone before a step could run
     */
    public void entityLoop(Entity entity, BooleanSupplier work, Runnable retired) {
        entity.getScheduler()
                .execute(
                        plugin,
                        () -> {
                            if (work.getAsBoolean()) entityLoop(entity, work, retired);
                        },
                        retired,
                        1);
    }

    /**
     * Runs work on the thread owning a location every tick, starting with the next one, for as long as it returns
     * true. Each run schedules the next one, so stopping needs no handle to cancel.
     *
     * @param work one step; false stops the loop
     */
    public void locationLoop(Location location, BooleanSupplier work) {
        server.getRegionScheduler()
                .runDelayed(
                        plugin,
                        location,
                        task -> {
                            if (work.getAsBoolean()) locationLoop(location, work);
                        },
                        1);
    }

    /**
     * Runs work on the thread owning a location: right away when the current thread owns it, otherwise on that
     * region's next tick.
     */
    public void at(Location location, Runnable work) {
        if (server.isOwnedByCurrentRegion(location)) work.run();
        else server.getRegionScheduler().execute(plugin, location, work);
    }

    /**
     * Stops every global task of the plugin and the packet sender, e.g. when it disables. Entity tasks end with their entities or by
     * cancelling them.
     */
    public synchronized void cancelAll() {
        server.getGlobalRegionScheduler().cancelTasks(plugin);
        server.getAsyncScheduler().cancelTasks(plugin);
        if (sender != null) sender.shutdownNow();
        sender = null;
    }

    /**
     * Sends packets off the tick threads, one after another in the order they were handed over, so particles drawn
     * in one tick leave together. The thread starts on first use.
     */
    public synchronized void send(Runnable work) {
        if (sender == null)
            sender = Executors.newSingleThreadExecutor(
                    Thread.ofPlatform().name("bloodnight-particles").daemon().factory());
        sender.execute(work);
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
