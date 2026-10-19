/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltInEffectsTest {
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void everyBuiltInEffectReadsAndPassesTheChecks() {
        List<String> problems = new ArrayList<>();
        BuiltInEffects.all()
                .forEach((id, effect) ->
                        problems.addAll(EffectChecks.problems(id, effect, key -> Material.matchMaterial(key) != null)));
        assertEquals(List.of(), problems);
        assertEquals(26, BuiltInEffects.all().size());
        assertTrue(BuiltInEffects.all().keySet().stream().allMatch(BuiltInEffects::builtIn));
    }

    @Test
    void ownEffectsAndBuiltInsAreLookedUpTogether() {
        EffectLibrary library = new EffectLibrary(ParticleEffects::new);
        assertTrue(library.knows("bloodnight:blood_aura"));
        assertTrue(!library.knows("blood_aura"));
    }
}
