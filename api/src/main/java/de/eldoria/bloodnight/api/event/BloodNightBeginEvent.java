/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event;

import org.bukkit.World;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;

/**
 * A blood night is about to begin in a world. Cancelling it keeps the night an ordinary one.
 */
public final class BloodNightBeginEvent extends WorldEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final long night;
    private boolean cancelled;

    /**
     * @param world the world
     * @param night the night's number: the day it begins on, {@code fullTime / 24000}
     */
    public BloodNightBeginEvent(World world, long night) {
        super(world);
        this.night = night;
    }

    /**
     * @return the night's number: the day it begins on, {@code fullTime / 24000}
     */
    public long night() {
        return night;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
