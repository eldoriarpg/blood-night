/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * What other plugins can ask BloodNight and tell it to do. Get it from Paper's services manager or through
 * {@link BloodNightProvider#get()}.
 * <p>
 * Worlds without a day cycle, such as the Nether and the End, have timed blood nights; their nights are numbered by
 * period instead of by day. On Folia, {@link #force}, {@link #cancel} and {@link #spawn} must be called on the thread
 * that owns what they change: the global region for the first two, the location's region for the last.
 */
public interface BloodNightService {
    /**
     * @return whether a blood night runs in the world
     */
    boolean isRunning(World world);

    /**
     * @return the blood night running in the world, if one does
     */
    Optional<RunningNight> running(World world);

    /**
     * @param horizon how many nights to look ahead at most
     * @return the next blood night of the world, from the one running or about to begin on
     */
    Optional<NightForecast> next(World world, int horizon);

    /**
     * @param nightsAhead 0 for the coming night (or the running one), 1 for the one after, and so on
     * @return how likely that night is a blood night, 0 to 100; forced nights count as 100, cancelled ones as 0
     */
    int probability(World world, int nightsAhead);

    /**
     * Makes the world's coming night a blood night; when it is night already, the blood night begins now.
     */
    void force(World world);

    /**
     * Keeps the world's coming night an ordinary one; a blood night running in it ends now.
     */
    void cancel(World world);

    /**
     * @return every world with a running blood night
     */
    Set<World> runningWorlds();

    /**
     * @return whether the entity is a BloodNight mob, or the entity riding or carrying one
     */
    boolean isCustomMob(Entity entity);

    /**
     * @return the id of the BloodNight mob the entity is, or belongs to as its rider or carrier
     */
    Optional<String> customMobId(Entity entity);

    /**
     * @return the ids of every BloodNight mob defined on the server
     */
    Set<String> customMobIds();

    /**
     * Spawns a BloodNight mob, as {@code /bloodnight spawn} does.
     *
     * @param mobId    a mob's id
     * @param location where it appears
     * @return the mob, or empty when no mob has that id or its entity type is not a living one
     */
    Optional<LivingEntity> spawn(String mobId, Location location);

    /**
     * @param mobId a mob's id
     * @return the mob, if the server has one with that id
     */
    Optional<MobInfo> mob(String mobId);

    /**
     * @param entity any entity
     * @return the special mob the entity is, if it is one
     */
    Optional<MobInfo> mob(Entity entity);

    /**
     * @return every special mob the server has
     */
    Collection<MobInfo> mobs();

    /**
     * @param world a world
     * @param mobId a mob's id
     * @return how often the mob is picked in that world compared to the others; 0 when it is switched off there or
     *     there is no such mob
     */
    int weight(World world, String mobId);

    /**
     * @param world a world
     * @return whether the world has blood nights at all
     */
    boolean isEnabled(World world);

    /**
     * @param world a world
     * @return how the world has blood nights
     */
    WorldInfo world(World world);
}
