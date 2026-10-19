/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.piece;

import de.eldoria.bloodnight.particles.effect.Piece;
import de.eldoria.bloodnight.particles.geometry.Frame;
import org.bukkit.util.Transformation;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The size and turn of a piece's display, from the frame it faces in.
 * <p>
 * A frame's axes (right, up, forward) are mirrored against the world's, so the display is turned by left, up and
 * forward instead; for the display that only swaps which side is left.
 */
final class PieceShapes {
    private static final float GONE = 0.001f;

    private PieceShapes() {}

    /**
     * @return the turn of a display facing the frame, with the piece's own fixed turn on top
     */
    static Quaternionf turn(Frame frame, Piece.Rotation rotation) {
        Matrix3f axes = new Matrix3f(
                (float) -frame.right().x(),
                (float) -frame.right().y(),
                (float) -frame.right().z(),
                (float) frame.up().x(),
                (float) frame.up().y(),
                (float) frame.up().z(),
                (float) frame.forward().x(),
                (float) frame.forward().y(),
                (float) frame.forward().z());
        return new Quaternionf()
                .setFromNormalized(axes)
                .rotateX((float) Math.toRadians(rotation.pitch()))
                .rotateY((float) Math.toRadians(rotation.yaw()))
                .rotateZ((float) Math.toRadians(rotation.roll()));
    }

    /**
     * @param block whether it shows a block, whose model starts at a corner instead of the middle
     * @return a display turned and scaled about its position
     */
    static Transformation centred(Quaternionf turn, float scale, boolean block) {
        Vector3f shift = block ? turn.transform(new Vector3f(-scale / 2, -scale / 2, -scale / 2)) : new Vector3f();
        return new Transformation(shift, turn, new Vector3f(scale), new Quaternionf());
    }

    /**
     * @param turn   the turn of a frame facing from one end to the other
     * @param width  the thickness
     * @param length the length
     * @return a block stretched from the display's position along the frame's forward axis
     */
    static Transformation span(Quaternionf turn, float width, float length) {
        Vector3f shift = turn.transform(new Vector3f(-width / 2, -width / 2, 0));
        return new Transformation(shift, turn, new Vector3f(width, width, Math.max(length, GONE)), new Quaternionf());
    }

    /**
     * @return the same display shrunk to nothing, to fade it out
     */
    static Transformation shrunk(Transformation shown) {
        return new Transformation(
                shown.getTranslation(), shown.getLeftRotation(), new Vector3f(GONE), shown.getRightRotation());
    }
}
