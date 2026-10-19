/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.effect;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.eldoria.bloodnight.particles.geometry.Facing;
import de.eldoria.bloodnight.particles.geometry.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A display entity inside an effect: solid things particles cannot draw, such as a spear, a blade or a glowing bar.
 * A missing field takes its default.
 *
 * @param kind     what it shows
 * @param item     item id or material key, for {@link PieceKind#ITEM}; an item from the registry keeps its resource
 *                 pack model
 * @param block    block key, for {@link PieceKind#BLOCK} and {@link PieceKind#SPAN}
 * @param scale    its size, 1 for a normal block or item
 * @param rotation a fixed turn on top of its facing, e.g. so a trident's tip points forward
 * @param anchor   where it sits; never on a piece
 * @param offset   shift from the anchor in blocks, as x right, y up, z forward
 * @param facing   how it is turned
 * @param motion   movement from its end to the other one; none stays at the anchor for the play
 * @param stick    ticks it stays where its motion ended, at most {@value #MAX_STICK}
 * @param glow     whether it has a glowing outline
 * @param color    the outline's colour as {@code #rrggbb}
 * @param width    thickness of a span in blocks
 * @param grow     ticks a span takes to reach from source to target
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Piece(
        PieceKind kind,
        @Nullable String item,
        @Nullable String block,
        Double scale,
        Rotation rotation,
        Anchor anchor,
        Vec3 offset,
        Facing facing,
        @Nullable Motion motion,
        Integer stick,
        Boolean glow,
        @Nullable String color,
        Double width,
        Integer grow) {
    /** The longest a piece stays where its motion ended. */
    public static final int MAX_STICK = 200;

    /** What a piece shows. */
    public enum PieceKind {
        /** An item, e.g. a trident. */
        ITEM,
        /** A block, scaled as it says. */
        BLOCK,
        /** A block stretched from source to target, kept stretched as both move. */
        SPAN
    }

    /**
     * A fixed turn in degrees.
     *
     * @param pitch about the side axis, tipping it forward
     * @param yaw   about the up axis
     * @param roll  about the forward axis
     */
    public record Rotation(double pitch, double yaw, double roll) {
        /** No turn. */
        public static final Rotation NONE = new Rotation(0, 0, 0);
    }

    public Piece {
        kind = Objects.requireNonNullElse(kind, PieceKind.ITEM);
        scale = Math.clamp(Objects.requireNonNullElse(scale, 1.0), 0.05, 8);
        rotation = Objects.requireNonNullElse(rotation, Rotation.NONE);
        anchor = Objects.requireNonNullElse(anchor, Anchor.SOURCE_CENTER);
        offset = Objects.requireNonNullElse(offset, Vec3.ZERO);
        facing = Objects.requireNonNullElse(facing, Facing.PATH);
        stick = Math.clamp(Objects.requireNonNullElse(stick, 20), 0, MAX_STICK);
        glow = Objects.requireNonNullElse(glow, false);
        width = Math.clamp(Objects.requireNonNullElse(width, 0.1), 0.01, 2);
        grow = Math.clamp(Objects.requireNonNullElse(grow, 0), 0, ParticleEffect.MAX_DURATION);
    }
}
