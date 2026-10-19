/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs.movement;

import de.eldoria.bloodnight.api.internal.MovementPreset;
import de.eldoria.bloodnight.mob.meta.Movement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseTest {
    private static Course.Sight at(double distance) {
        return new Course.Sight(distance, 1, 1, true, false, false, 0);
    }

    private static Course course(MovementPreset preset, Course.Sight sight) {
        return Course.of(Movement.of(preset), sight);
    }

    @Test
    void keepingDistanceBacksOffHoldsAndCloses() {
        assertEquals(Course.RETREAT, course(MovementPreset.KEEP_DISTANCE, at(3)));
        assertEquals(Course.HOLD, course(MovementPreset.KEEP_DISTANCE, at(8)));
        assertEquals(Course.APPROACH, course(MovementPreset.KEEP_DISTANCE, at(15)));
        assertEquals(Course.NONE, course(MovementPreset.KEEP_DISTANCE, at(Double.NaN)));
    }

    @Test
    void hitAndRunBacksOffOnlyAfterAHit() {
        assertEquals(Course.NONE, course(MovementPreset.HIT_AND_RUN, at(2)));
        assertEquals(
                Course.RETREAT, course(MovementPreset.HIT_AND_RUN, new Course.Sight(2, 1, 1, true, true, false, 0)));
    }

    @Test
    void aStalkerClosesInWhenNotWatchedOrWhenTheTargetIsWeak() {
        assertEquals(Course.RETREAT, course(MovementPreset.STALKER, at(3)));
        assertEquals(Course.NONE, course(MovementPreset.STALKER, new Course.Sight(3, 1, 1, false, false, false, 0)));
        assertEquals(Course.NONE, course(MovementPreset.STALKER, new Course.Sight(3, 1, 0.1, true, false, false, 0)));
    }

    @Test
    void aCowardFleesBelowItsShareAndReturnsAtTwiceIt() {
        Movement coward = Movement.of(MovementPreset.COWARD);
        assertTrue(Course.flees(coward, new Course.Sight(5, 0.2, 1, true, false, false, 0)));
        assertTrue(Course.flees(coward, new Course.Sight(5, 0.5, 1, true, false, true, 0)));
        assertFalse(Course.flees(coward, new Course.Sight(5, 0.7, 1, true, false, true, 0)));
        assertFalse(Course.flees(coward, new Course.Sight(5, 0.5, 1, true, false, false, 0)));
    }

    @Test
    void aGuardReturnsHomeOutsideItsRange() {
        assertEquals(Course.HOME, course(MovementPreset.GUARD, new Course.Sight(5, 1, 1, true, false, false, 30)));
        assertEquals(Course.NONE, course(MovementPreset.GUARD, new Course.Sight(5, 1, 1, true, false, false, 10)));
    }

    @Test
    void vanillaAndTheTargetingPresetsLeaveMovingToTheVanillaAi() {
        assertEquals(Course.NONE, course(MovementPreset.VANILLA, at(3)));
        assertEquals(Course.NONE, course(MovementPreset.AGGRESSIVE, at(3)));
        assertEquals(Course.NONE, course(MovementPreset.PACK, at(3)));
    }
}
