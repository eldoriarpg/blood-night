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

import java.time.Duration;
import java.util.concurrent.TimeUnit;
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
     * Runs work off the server's threads every {@code period}, starting right away, e.g. to ask a web service.
     */
    public ScheduledTask asyncTimer(Duration period, Runnable work) {
        return server.getAsyncScheduler()
                .runAtFixedRate(plugin, task -> work.run(), 0, period.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Runs work on the entity's thread on its next tick; nothing runs when the entity is gone by then.
     */
    public void entity(Entity entity, Runnable work) {
        entity.getScheduler().execute(plugin, work, null, 1);
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
     * Runs work on the thread owning a location: right away when the current thread owns it, otherwise on that
     * region's next tick.
     */
    public void at(Location location, Runnable work) {
        if (server.isOwnedByCurrentRegion(location)) work.run();
        else server.getRegionScheduler().execute(plugin, location, work);
    }

    /**
     * Stops every global task of the plugin, e.g. when it disables. Entity tasks end with their entities or by
     * cancelling them.
     */
    public void cancelAll() {
        server.getGlobalRegionScheduler().cancelTasks(plugin);
        server.getAsyncScheduler().cancelTasks(plugin);
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
