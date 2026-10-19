/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.geometry;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShapesTest {
    private static final double EPSILON = 1e-9;
    private static final Frame SOUTH = Frame.of(Vec3.ZERO, new Vec3(0, 0, 1));

    private static Shape shape(Shape.ShapeType type, Facing facing) {
        return new Shape(
                type, 2.0, 8, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                facing);
    }

    @Test
    void ringFacingUpLiesFlatAtItsRadius() {
        List<Vec3> points = Shapes.around(shape(Shape.ShapeType.RING, Facing.UP), SOUTH, Moment.START);
        assertEquals(8, points.size());
        for (Vec3 point : points) {
            assertEquals(0, point.y(), EPSILON);
            assertEquals(2, point.length(), EPSILON);
        }
    }

    @Test
    void ringFacingPathStandsAcrossTheLine() {
        Frame east = Frame.of(Vec3.ZERO, new Vec3(1, 0, 0));
        for (Vec3 point : Shapes.around(shape(Shape.ShapeType.RING, Facing.PATH), east, Moment.START)) {
            assertEquals(0, point.x(), EPSILON);
            assertEquals(2, point.length(), EPSILON);
        }
    }

    @Test
    void spinTurnsTheRingWithEveryEmission() {
        Shape spinning = new Shape(
                Shape.ShapeType.RING,
                1.0,
                4,
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
                null,
                90.0,
                null,
                Facing.UP);
        Vec3 first = Shapes.around(spinning, SOUTH, Moment.START).getFirst();
        Vec3 next = Shapes.around(spinning, SOUTH, new Moment(0, 1, 0)).getFirst();
        assertEquals(Shapes.around(spinning, SOUTH, Moment.START).get(1).x(), next.x(), EPSILON);
        assertEquals(Shapes.around(spinning, SOUTH, Moment.START).get(1).z(), next.z(), EPSILON);
        assertTrue(first.minus(next).length() > 0.1);
    }

    @Test
    void growingShapesStartFromNothing() {
        Shape growing = new Shape(
                Shape.ShapeType.RING,
                3.0,
                8,
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
                null,
                null,
                true,
                Facing.UP);
        assertEquals(
                1.5,
                Shapes.around(growing, SOUTH, new Moment(0.5, 0, 0)).getFirst().length(),
                EPSILON);
        assertEquals(
                3, Shapes.around(growing, SOUTH, new Moment(1, 0, 0)).getFirst().length(), EPSILON);
    }

    @Test
    void sphereKeepsItsRadius() {
        for (Vec3 point : Shapes.around(shape(Shape.ShapeType.SPHERE, Facing.UP), SOUTH, Moment.START)) {
            assertEquals(2, point.length(), EPSILON);
        }
    }

    @Test
    void spiralRisesWithEachStrand() {
        Shape helix = new Shape(
                Shape.ShapeType.SPIRAL,
                1.0,
                10,
                null,
                3.0,
                2.0,
                2,
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
                Facing.UP);
        List<Vec3> points = Shapes.around(helix, SOUTH, Moment.START);
        assertEquals(20, points.size());
        assertEquals(0, points.getFirst().y(), EPSILON);
        assertEquals(3, points.get(9).y(), EPSILON);
        assertEquals(-points.getFirst().x(), points.get(10).x(), EPSILON);
    }

    @Test
    void lineRunsToItsEndInTheFrame() {
        Shape line = new Shape(
                Shape.ShapeType.LINE,
                null,
                3,
                new Vec3(0, 0, 4),
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
                null,
                null,
                null);
        List<Vec3> points = Shapes.around(line, SOUTH, Moment.START);
        assertEquals(new Vec3(0, 0, 0), points.getFirst());
        assertEquals(4, points.getLast().z(), EPSILON);
    }

    @Test
    void mirroredPatternIsCopiedToTheOtherSide() {
        Shape wings = new Shape(
                Shape.ShapeType.PATTERN,
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
                List.of("x.", ".x"),
                0.5,
                true,
                null,
                null,
                null,
                Facing.BODY);
        List<Vec3> points = Shapes.around(wings, SOUTH, Moment.START);
        assertEquals(4, points.size());
        assertEquals(-points.get(0).x(), points.get(1).x(), EPSILON);
        assertEquals(0.25, points.get(0).y(), EPSILON);
        assertEquals(0, points.get(0).z(), EPSILON);
        assertEquals(4, Shapes.count(wings));
    }

    @Test
    void flappingSwingsBothHalvesTheSameWay() {
        Shape wings = new Shape(
                Shape.ShapeType.PATTERN,
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
                List.of("x"),
                1.0,
                true,
                new Shape.Flap(90, 4),
                null,
                null,
                Facing.BODY);
        List<Vec3> points = Shapes.around(wings, SOUTH, new Moment(0, 0, 1));
        assertEquals(points.get(0).z(), points.get(1).z(), EPSILON);
        assertEquals(-0.5, points.get(0).z(), EPSILON);
        assertEquals(0, points.get(0).x(), EPSILON);
    }

    @Test
    void beamRunsFromEndToEnd() {
        Shape beam = shape(Shape.ShapeType.BEAM, Facing.PATH);
        List<Vec3> points = Shapes.between(beam, Vec3.ZERO, new Vec3(4, 0, 0), Moment.START, new Random(1));
        assertEquals(17, points.size());
        assertEquals(Vec3.ZERO, points.getFirst());
        assertEquals(4, points.getLast().x(), EPSILON);
    }

    @Test
    void beamsBeyondTheLongestSpanAreNotDrawn() {
        Shape beam = shape(Shape.ShapeType.BEAM, Facing.PATH);
        assertTrue(Shapes.between(beam, Vec3.ZERO, new Vec3(40, 0, 0), Moment.START, new Random(1))
                .isEmpty());
    }

    @Test
    void arcPeaksAtItsBend() {
        Shape arc = new Shape(
                Shape.ShapeType.ARC,
                null,
                null,
                null,
                null,
                null,
                null,
                4.0,
                null,
                1.5,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        List<Vec3> points = Shapes.between(arc, Vec3.ZERO, new Vec3(4, 0, 0), Moment.START, new Random(1));
        double highest = points.stream().mapToDouble(Vec3::y).max().orElse(0);
        assertEquals(1.5, highest, EPSILON);
    }

    @Test
    void growingBeamReachesPartWay() {
        Shape beam = new Shape(
                Shape.ShapeType.BEAM,
                null,
                null,
                null,
                null,
                null,
                null,
                4.0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                null);
        List<Vec3> points = Shapes.between(beam, Vec3.ZERO, new Vec3(4, 0, 0), new Moment(0.5, 0, 0), new Random(1));
        assertEquals(2, points.getLast().x(), EPSILON);
    }
}
