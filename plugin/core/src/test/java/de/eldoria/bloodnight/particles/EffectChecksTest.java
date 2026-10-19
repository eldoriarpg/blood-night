/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles;

import de.eldoria.bloodnight.particles.data.ParticleDataSpec;
import de.eldoria.bloodnight.particles.effect.Anchor;
import de.eldoria.bloodnight.particles.effect.Emitter;
import de.eldoria.bloodnight.particles.effect.ParticleEffect;
import de.eldoria.bloodnight.particles.effect.Piece;
import de.eldoria.bloodnight.particles.geometry.Shape;
import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EffectChecksTest {
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static List<String> problems(ParticleEffect effect) {
        return EffectChecks.problems("test", effect, key -> Material.matchMaterial(key) != null);
    }

    private static Emitter dust(ParticleDataSpec data) {
        return new Emitter("minecraft:dust", data, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void unknownParticlesAreRefused() {
        ParticleEffect effect =
                new ParticleEffect("x", null, List.of(Emitter.of("minecraft:nothing", Shape.POINT)), null, null);
        assertEquals(
                List.of("Particle effect test, emitter 1 uses unknown particle minecraft:nothing"), problems(effect));
    }

    @Test
    void dustNeedsAColour() {
        ParticleEffect effect = new ParticleEffect("x", null, List.of(dust(ParticleDataSpec.NONE)), null, null);
        assertTrue(problems(effect).getFirst().contains("needs a colour"));
        ParticleDataSpec red = new ParticleDataSpec("#ff0000", null, 1.0, null, null, null, null, null, null);
        assertEquals(List.of(), problems(new ParticleEffect("x", null, List.of(dust(red)), null, null)));
    }

    @Test
    void tooManyEmittersAreRefused() {
        List<Emitter> many = Collections.nCopies(9, Emitter.of("minecraft:flame", Shape.POINT));
        assertEquals(
                List.of("Particle effect test has more than 8 emitters"),
                problems(new ParticleEffect("x", null, many, null, null)));
    }

    @Test
    void emittersOnMissingPiecesAndPiecesWithoutItemsAreRefused() {
        Emitter onPiece = new Emitter(
                "minecraft:flame",
                null,
                new Anchor(null, Anchor.EffectEnd.PIECE, 1),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        Piece piece = new Piece(
                Piece.PieceKind.ITEM,
                "minecraft:nothing",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        List<String> problems = problems(new ParticleEffect("x", null, List.of(onPiece), List.of(piece), null));
        assertEquals(2, problems.size());
        assertTrue(problems.get(0).contains("sits on piece 2"));
        assertTrue(problems.get(1).contains("unknown item minecraft:nothing"));
    }
}
