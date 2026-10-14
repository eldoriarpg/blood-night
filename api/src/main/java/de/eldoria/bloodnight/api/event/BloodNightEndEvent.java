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
 * A blood night has ended in a world, see {@link #reason()} for why.
 */
public final class BloodNightEndEvent extends WorldEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final long night;
    private final EndReason reason;

    /**
     * @param world  the world
     * @param night  the night's number
     * @param reason why it ended
     */
    public BloodNightEndEvent(World world, long night, EndReason reason) {
        super(world);
        this.night = night;
        this.reason = reason;
    }

    /**
     * @return why the blood night ended
     */
    public EndReason reason() {
        return reason;
    }

    /**
     * @return the night's number: the day it begins on, or the period for worlds without a day cycle
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
