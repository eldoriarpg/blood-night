/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event;

import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;

/**
 * A blood night has ended in a world: the night is over, or it was cancelled while running.
 */
public final class BloodNightEndEvent extends WorldEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final long night;

    /**
     * @param world the world
     * @param night the night's number: the day it begins on, {@code fullTime / 24000}
     */
    public BloodNightEndEvent(World world, long night) {
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
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
