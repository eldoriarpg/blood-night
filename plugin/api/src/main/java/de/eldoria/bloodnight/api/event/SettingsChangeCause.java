/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event;

/**
 * What made BloodNight read its settings again.
 */
public enum SettingsChangeCause {
    /** {@code /bloodnight reload}. */
    RELOAD,
    /** A web editor session was applied. */
    EDITOR,
    /** {@code /bloodnight settings} changed a setting. */
    COMMAND,
    /** Mobs were installed or updated from a store. */
    STORE,
    /** The old BloodNight's settings were taken over with {@code /bloodnight convert}. */
    CONVERT
}
