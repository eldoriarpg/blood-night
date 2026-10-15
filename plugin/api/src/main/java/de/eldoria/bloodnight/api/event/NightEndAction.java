/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event;

/**
 * What happens to a special mob once the blood night it belongs to is over.
 */
public enum NightEndAction {
    /** The mob goes away. */
    REMOVE,
    /** The mob turns into an ordinary one of its kind. */
    REVERT,
    /** The mob stays as it is, with its behaviour. */
    KEEP
}
