/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event.mob;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.jspecify.annotations.Nullable;

/**
 * A BloodNight mob's behaviour is about to move an entity: teleport it somewhere, or pick it up and carry it. Cancelling
 * it leaves the entity where it is, e.g. for a player whose boots keep them on the ground.
 * <p>
 * Called on the moved entity's thread. Also called when a mob moves itself.
 */
public final class CustomMobMoveEntityEvent extends EntityEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Kind kind;
    private final @Nullable Location to;
    private boolean cancelled;

    /**
     * How the entity is moved.
     */
    public enum Kind {
        /** Teleported to {@link #to()}. */
        TELEPORT,
        /** Picked up by another entity and carried. */
        CARRY
    }

    /**
     * @param entity the entity that is moved
     * @param kind   how it is moved
     * @param to     where it is teleported to; {@code null} when it is carried
     */
    public CustomMobMoveEntityEvent(Entity entity, Kind kind, @Nullable Location to) {
        super(entity);
        this.kind = kind;
        this.to = to;
    }

    /**
     * @return how the entity is moved
     */
    public Kind kind() {
        return kind;
    }

    /**
     * @return where the entity is teleported to; {@code null} when it is carried
     */
    public @Nullable Location to() {
        return to == null ? null : to.clone();
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
