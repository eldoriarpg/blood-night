/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.piece;

import de.eldoria.bloodnight.particles.effect.Piece;
import de.eldoria.bloodnight.particles.geometry.Frame;
import de.eldoria.bloodnight.particles.geometry.Vec3;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PieceShapesTest {
    private static final float EPSILON = 1e-5f;

    @Test
    void aDisplayFacingAFrameLooksAlongItsForwardAxis() {
        Frame east = Frame.of(Vec3.ZERO, new Vec3(1, 0, 0));
        Vector3f ahead = PieceShapes.turn(east, Piece.Rotation.NONE).transform(new Vector3f(0, 0, 1));
        assertEquals(1, ahead.x, EPSILON);
        assertEquals(0, ahead.y, EPSILON);
        assertEquals(0, ahead.z, EPSILON);
    }

    @Test
    void aSpanReachesItsLengthAlongTheFrame() {
        Frame east = Frame.of(Vec3.ZERO, new Vec3(1, 0, 0));
        Quaternionf turn = PieceShapes.turn(east, Piece.Rotation.NONE);
        Transformation span = PieceShapes.span(turn, 0.2f, 5);
        Vector3f far = new Vector3f(0.5f, 0.5f, 1)
                .mul(span.getScale())
                .rotate(span.getLeftRotation())
                .add(span.getTranslation());
        assertEquals(5, far.x, EPSILON);
        assertEquals(0, far.y, EPSILON);
        assertEquals(0, far.z, EPSILON);
    }
}
