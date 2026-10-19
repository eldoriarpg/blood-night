/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.geometry;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Where the particles of one emission appear, around the emitter's anchor. Only the fields of the shape's type are
 * used; a missing field takes its default, so a file only needs the ones it changes.
 *
 * @param type    the kind of shape
 * @param radius  ring, sphere and spiral radius in blocks
 * @param points  how many points a ring, sphere or spiral has, at most {@value Shapes#MAX_POINTS}
 * @param to      where a line ends, from its start, as x right, y up, z forward
 * @param height  how high a spiral rises
 * @param turns   how often a spiral goes round
 * @param strands how many coils a spiral has; 2 is a helix
 * @param density points per block along a beam or arc
 * @param jitter  how far beam and arc points stray sideways at random, for a crackling bolt
 * @param bend    how high an arc curves above the straight line, in blocks
 * @param twist   degrees the arc's curve is turned about the line, e.g. 180 for one curving the other way
 * @param rows    a picture for {@link ShapeType#PATTERN}, top row first, {@code x} for a point and {@code .} for none
 * @param scale   blocks between two cells of the picture
 * @param mirror  whether the picture is copied to the other side, its left column at the middle
 * @param flap    a swing of the picture's two halves about the middle, for wings
 * @param spin    degrees the shape turns about its axis at every emission
 * @param grow    whether the shape grows from nothing to its size over the play; beams and arcs grow in length
 * @param facing  how the shape is turned
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Shape(
        ShapeType type,
        Double radius,
        Integer points,
        Vec3 to,
        Double height,
        Double turns,
        Integer strands,
        Double density,
        Double jitter,
        Double bend,
        Double twist,
        List<String> rows,
        Double scale,
        Boolean mirror,
        @Nullable Flap flap,
        Double spin,
        Boolean grow,
        Facing facing) {
    /** A single point at the anchor. */
    public static final Shape POINT = new Shape(
            ShapeType.POINT,
            1.0,
            16,
            new Vec3(0, 1, 0),
            2.0,
            2.0,
            1,
            4.0,
            0.0,
            1.0,
            0.0,
            List.of(),
            0.15,
            false,
            null,
            0.0,
            false,
            Facing.UP);

    /** The kinds of shape. */
    public enum ShapeType {
        /** The anchor itself. */
        POINT,
        /** Evenly on a flat circle. */
        RING,
        /** Evenly on a sphere's surface. */
        SPHERE,
        /** Evenly from the anchor to {@code to}. */
        LINE,
        /** A rising coil; more strands wind around each other. */
        SPIRAL,
        /** Straight from source to target. */
        BEAM,
        /** Curved from source to target. */
        ARC,
        /** The points of a small picture. */
        PATTERN
    }

    /**
     * A swing of a picture's halves, forward and back like a wing beat.
     *
     * @param degrees how far each half swings to either side
     * @param ticks   how long one beat takes
     */
    public record Flap(double degrees, int ticks) {
        public Flap {
            ticks = Math.max(2, ticks);
        }
    }

    public Shape {
        type = Objects.requireNonNullElse(type, ShapeType.POINT);
        radius = Math.max(0, Objects.requireNonNullElse(radius, 1.0));
        points = Math.clamp(Objects.requireNonNullElse(points, 16), 1, Shapes.MAX_POINTS);
        to = Objects.requireNonNullElse(to, new Vec3(0, 1, 0));
        height = Objects.requireNonNullElse(height, 2.0);
        turns = Objects.requireNonNullElse(turns, 2.0);
        strands = Math.clamp(Objects.requireNonNullElse(strands, 1), 1, 8);
        density = Math.clamp(Objects.requireNonNullElse(density, 4.0), 0.1, 16);
        jitter = Math.max(0, Objects.requireNonNullElse(jitter, 0.0));
        bend = Objects.requireNonNullElse(bend, 1.0);
        twist = Objects.requireNonNullElse(twist, 0.0);
        rows = List.copyOf(Objects.requireNonNullElse(rows, List.of()));
        scale = Math.max(0.01, Objects.requireNonNullElse(scale, 0.15));
        mirror = Objects.requireNonNullElse(mirror, false);
        spin = Objects.requireNonNullElse(spin, 0.0);
        grow = Objects.requireNonNullElse(grow, false);
        facing = Objects.requireNonNullElse(facing, Facing.UP);
    }

    /**
     * @return whether the shape runs between source and target, so it needs both
     */
    public boolean betweenEnds() {
        return type == ShapeType.BEAM || type == ShapeType.ARC;
    }
}
