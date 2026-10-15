/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs.movement;

import de.eldoria.bloodnight.mob.meta.Movement;

/**
 * What a mob's movement asks it to do this moment, decided from what it sees; {@link #NONE} leaves it to the vanilla
 * AI.
 */
public enum Course {
    /**
     * Leave the moving to the vanilla AI.
     */
    NONE,
    /**
     * Walk towards the target.
     */
    APPROACH,
    /**
     * Walk away from the target.
     */
    RETREAT,
    /**
     * Stand still.
     */
    HOLD,
    /**
     * Walk back to where the mob spawned.
     */
    HOME;

    /**
     * What the mob sees.
     *
     * @param distance     blocks to its target; {@link Double#NaN} without one
     * @param health       its own health share, 0 to 1
     * @param targetHealth its target's health share, 0 to 1
     * @param watched      whether the target looks at it
     * @param backingOff   whether a hit-and-run mob is still backing off after a hit
     * @param fleeing      whether a coward is fleeing already
     * @param fromHome     blocks from where it spawned
     */
    public record Sight(
            double distance,
            double health,
            double targetHealth,
            boolean watched,
            boolean backingOff,
            boolean fleeing,
            double fromHome) {
        boolean hasTarget() {
            return !Double.isNaN(distance);
        }
    }

    /**
     * @param movement the mob's movement
     * @param sight    what it sees
     * @return what to do
     */
    public static Course of(Movement movement, Sight sight) {
        return switch (movement.preset()) {
            case VANILLA, AGGRESSIVE, PACK -> NONE;
            case KEEP_DISTANCE -> sight.hasTarget() ? keep(movement, sight.distance()) : NONE;
            case HIT_AND_RUN -> sight.hasTarget() && sight.backingOff() ? RETREAT : NONE;
            case STALKER -> stalk(movement, sight);
            case COWARD -> sight.hasTarget() && flees(movement, sight) ? RETREAT : NONE;
            case GUARD -> sight.fromHome() > movement.range() ? HOME : NONE;
        };
    }

    /**
     * @param movement a coward's movement
     * @param sight    what it sees
     * @return whether it flees: below its threshold, and once fleeing until it has twice that again
     */
    public static boolean flees(Movement movement, Sight sight) {
        double back = Math.min(1, movement.fleeBelow() * 2);
        return sight.fleeing() ? sight.health() < back : sight.health() < movement.fleeBelow();
    }

    private static Course stalk(Movement movement, Sight sight) {
        if (!sight.hasTarget() || !sight.watched() || sight.targetHealth() < movement.fleeBelow()) return NONE;
        return keep(movement, sight.distance());
    }

    private static Course keep(Movement movement, double distance) {
        if (distance < movement.closest()) return RETREAT;
        if (distance > movement.farthest()) return APPROACH;
        return HOLD;
    }
}
