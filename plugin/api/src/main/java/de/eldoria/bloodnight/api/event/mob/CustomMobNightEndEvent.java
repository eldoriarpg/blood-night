/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event.mob;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

/**
 * The blood night a special mob belongs to is over, and the mob is about to be removed or turned into an ordinary one.
 * Changing the action decides otherwise; cancelling or {@link NightEndAction#KEEP} leaves the mob as it is for good.
 * <p>
 * Called on the mob's thread. Not called for mobs set to stay, which are kept anyway.
 */
public final class CustomMobNightEndEvent extends EntityEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String mobId;
    private NightEndAction action;
    private boolean cancelled;

    /**
     * @param entity the mob
     * @param mobId  its id
     * @param action what is about to happen to it
     */
    public CustomMobNightEndEvent(LivingEntity entity, String mobId, NightEndAction action) {
        super(entity);
        this.mobId = mobId;
        this.action = action;
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    /**
     * @return the mob's id
     */
    public String mobId() {
        return mobId;
    }

    /**
     * @return what happens to the mob
     */
    public NightEndAction action() {
        return action;
    }

    /**
     * @param action what happens to the mob instead
     */
    public void action(NightEndAction action) {
        this.action = action;
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
