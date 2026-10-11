/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.mob.meta.ValueModifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MobOutfitterTest {
    @Test
    void valuesFollowTheModifier() {
        assertEquals(40, MobOutfitter.value(20, ValueModifier.DEFAULT, 5, 2));
        assertEquals(100, MobOutfitter.value(20, ValueModifier.MULTIPLY, 5, 2));
        assertEquals(5, MobOutfitter.value(20, ValueModifier.VALUE, 5, 2));
        assertEquals(0, MobOutfitter.value(20, ValueModifier.VALUE, -5, 2));
    }
}
