/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event.mob;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityEvent;

/**
 * A spawning entity is about to become a BloodNight mob. Cancelling it keeps the entity as it is.
 * <p>
 * Called one tick after the entity spawned, on the entity's thread. Not called for mobs spawned by command.
 */
public final class CustomMobSpawnEvent extends EntityEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String mobId;
    private final CreatureSpawnEvent.SpawnReason reason;
    private boolean cancelled;

    /**
     * @param entity the spawning entity
     * @param mobId  the mob it becomes
     * @param reason why it spawned
     */
    public CustomMobSpawnEvent(LivingEntity entity, String mobId, CreatureSpawnEvent.SpawnReason reason) {
        super(entity);
        this.mobId = mobId;
        this.reason = reason;
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    /**
     * @return the id of the mob the entity becomes
     */
    public String mobId() {
        return mobId;
    }

    /**
     * @return why the entity spawned
     */
    public CreatureSpawnEvent.SpawnReason reason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
