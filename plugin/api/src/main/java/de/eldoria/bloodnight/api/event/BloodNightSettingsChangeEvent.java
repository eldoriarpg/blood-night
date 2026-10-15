/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * BloodNight's settings changed: worlds, mobs, items or the plugin's own settings may differ from before. Called after
 * the new settings are in use, on the global thread, so anything asked through the service now answers from them.
 */
public final class BloodNightSettingsChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final SettingsChangeCause cause;

    /**
     * @param cause what made the settings change
     */
    public BloodNightSettingsChangeEvent(SettingsChangeCause cause) {
        this.cause = cause;
    }

    /**
     * @return what made the settings change
     */
    public SettingsChangeCause cause() {
        return cause;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
