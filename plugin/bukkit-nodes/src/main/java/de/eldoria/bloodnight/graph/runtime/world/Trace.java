/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime.world;

import de.eldoria.bloodnight.graph.runtime.BukkitRuntime;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * What a line trace met: whether it hit anything, where, what, and how far from its start.
 *
 * @param hit      whether the line met a block or entity
 * @param location where it met it; missing without a hit
 * @param entity   the entity it met; missing when it met a block or nothing
 * @param block    the block it met; missing when it met an entity or nothing
 * @param distance how far from the start it met something, the full distance without a hit
 */
public record Trace(
        boolean hit,
        @Nullable Location location,
        @Nullable Entity entity,
        @Nullable Block block,
        double distance) {
    /**
     * Follows a line from a location, ignoring fluids and passable blocks such as grass. A line that could reach into
     * another region (on Folia) meets nothing.
     *
     * @param start     where the line starts
     * @param direction where it goes; a zero vector hits nothing
     * @param distance  how far it goes
     * @param ignore    an entity it passes through, e.g. the one looking; may be missing
     * @param hits      what it stops at
     * @return what it met
     */
    public static Trace of(Location start, Vector direction, double distance, @Nullable Entity ignore, TraceHits hits) {
        World world = start.getWorld();
        if (world == null || direction.lengthSquared() == 0 || distance <= 0) return miss(distance);
        if (!BukkitRuntime.owns(start, distance)) return miss(distance);
        Predicate<Entity> filter = entity -> !entity.equals(ignore);
        Vector normal = direction.clone().normalize();
        RayTraceResult result =
                switch (hits) {
                    case BLOCKS -> world.rayTraceBlocks(start, normal, distance, FluidCollisionMode.NEVER, true);
                    case ENTITIES -> world.rayTraceEntities(start, normal, distance, filter);
                    case BOTH -> world.rayTrace(start, normal, distance, FluidCollisionMode.NEVER, true, 0, filter);
                };
        if (result == null) return miss(distance);
        Vector position = Objects.requireNonNull(result.getHitPosition());
        return new Trace(
                true,
                position.toLocation(world),
                result.getHitEntity(),
                result.getHitBlock(),
                position.distance(start.toVector()));
    }

    private static Trace miss(double distance) {
        return new Trace(false, null, null, null, Math.max(0, distance));
    }
}
