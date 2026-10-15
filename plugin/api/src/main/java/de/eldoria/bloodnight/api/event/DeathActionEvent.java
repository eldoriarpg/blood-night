/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

/**
 * A death during a blood night is about to cause a lightning strike, thunder or a shockwave. Cancelling it leaves that
 * one out; the others happen as usual.
 * <p>
 * Called on the dying entity's thread, after its death event.
 */
public final class DeathActionEvent extends EntityEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final DeathAction action;
    private boolean cancelled;

    /**
     * @param entity the player or enemy that died
     * @param action what is about to happen
     */
    public DeathActionEvent(LivingEntity entity, DeathAction action) {
        super(entity);
        this.action = action;
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    /**
     * @return what is about to happen
     */
    public DeathAction action() {
        return action;
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
