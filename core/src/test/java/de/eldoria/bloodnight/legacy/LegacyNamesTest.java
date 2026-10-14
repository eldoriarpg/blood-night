/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.legacy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacyNamesTest {
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void soundsAreFoundByTheirOldNames() {
        assertEquals(
                Optional.of("minecraft:ambient.basalt_deltas.additions"),
                LegacyNames.sound("AMBIENT_BASALT_DELTAS_ADDITIONS"));
        assertEquals(
                Optional.of("minecraft:entity.lightning_bolt.thunder"),
                LegacyNames.sound("ENTITY_LIGHTNING_BOLT_THUNDER"));
        assertEquals(Optional.empty(), LegacyNames.sound("NO_SUCH_SOUND"));
    }

    @Test
    void effectsKeepOrChangeTheirNames() {
        assertEquals("minecraft:nausea", LegacyNames.effect("CONFUSION"));
        assertEquals("minecraft:blindness", LegacyNames.effect("BLINDNESS"));
    }

    @Test
    void oldMobNamesAreStoreIds() {
        assertEquals("speed_creeper", LegacyConverter.id("SpeedCreeper"));
        assertEquals("wither_skeleton_rider", LegacyConverter.id("WitherSkeletonRider"));
    }
}
