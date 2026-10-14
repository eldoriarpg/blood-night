/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event;

/**
 * Why a blood night ended.
 */
public enum EndReason {
    /**
     * Its time is over: sunrise, or the end of a timed night.
     */
    TIME,
    /**
     * It was cancelled, by command or through the service.
     */
    CANCELLED,
    /**
     * Its world no longer has blood nights.
     */
    DISABLED,
    /**
     * What held it is over, e.g. the dragon fight.
     */
    RELEASED,
    /**
     * The plugin stopped.
     */
    SHUTDOWN
}
