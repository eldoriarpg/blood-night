/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.internal;

import org.jetbrains.annotations.ApiStatus;

/**
 * How a custom mob moves, on top of its vanilla AI. Only mobs that find paths (zombies, skeletons, spiders, ...) follow
 * a preset; others keep moving as they do.
 * <p>
 * For the code BloodNight generates from mob graphs; may change in any version without notice.
 */
@ApiStatus.Internal
public enum MovementPreset {
    /**
     * Moves as the vanilla mob does.
     */
    VANILLA,
    /**
     * Hunts the nearest player within its range and does not give up the chase.
     */
    AGGRESSIVE,
    /**
     * Stays between its closest and farthest distance to its target, backing off when the target comes closer; for
     * mobs that shoot.
     */
    KEEP_DISTANCE,
    /**
     * Charges, hits once, then backs off for a few seconds before it comes back.
     */
    HIT_AND_RUN,
    /**
     * Follows its target at a distance and only closes in when the target looks away or is low on health.
     */
    STALKER,
    /**
     * Flees from its target when its own health is low, heals while it flees and comes back.
     */
    COWARD,
    /**
     * Takes over the target of mobs of its own kind nearby, so they attack together.
     */
    PACK,
    /**
     * Stays near where it spawned and only attacks what comes close.
     */
    GUARD
}
