/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

/**
 * How an installed mob stands to the store it came from.
 */
public enum StoreState {
    /** The server's own mob, from no store. */
    OWN,
    /** As installed, and the store has nothing newer. */
    CURRENT,
    /** As installed, and the store has a newer version; updates on its own. */
    OUTDATED,
    /** Its logic was changed since it was installed. */
    CHANGED,
    /** Its logic was changed, and the store has a newer version; updates only when asked. */
    CHANGED_OUTDATED,
    /** Its store or entry is gone. */
    ORPHANED
}
