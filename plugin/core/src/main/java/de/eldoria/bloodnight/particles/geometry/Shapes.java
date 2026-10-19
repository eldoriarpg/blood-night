/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Works out the points of a shape. The editor's preview does the same in TypeScript; a test fixture keeps both equal.
 * <p>
 * Flat shapes are laid out in a plane: across the frame (right and forward) when they face {@link Facing#UP}, upright
 * (right and up) otherwise; their axis is the remaining direction. Spinning turns a shape about that axis, growing
 * scales it from nothing at the play's start to its size at the end.
 */
public final class Shapes {
    /** The most points one shape has. */
    public static final int MAX_POINTS = 64;

    /** The longest beam or arc, in blocks; longer ones are not drawn. */
    public static final double MAX_SPAN = 32;

    /** The largest picture, in cells each way. */
    public static final int MAX_CELLS = 16;

    private static final double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));

    private Shapes() {}

    /**
     * @param shape  a shape that does not need both ends
     * @param frame  where it sits and how it is turned
     * @param moment when in the play it is drawn
     * @return the points in the world
     */
    public static List<Vec3> around(Shape shape, Frame frame, Moment moment) {
        double size = shape.grow() ? moment.progress() : 1;
        if (shape.type() == Shape.ShapeType.LINE) return line(shape, frame, size);
        double spin = Math.toRadians(shape.spin() * moment.emission());
        List<Vec3> points = new ArrayList<>();
        for (Vec3 plane : plane(shape, moment)) {
            Vec3 turned = turn(plane, spin).times(size);
            Vec3 local = shape.facing() == Facing.UP ? new Vec3(turned.x(), turned.z(), turned.y()) : turned;
            points.add(frame.world(local));
        }
        return points;
    }

    /**
     * @param shape  a beam or an arc
     * @param from   the source end
     * @param to     the target end
     * @param moment when in the play it is drawn
     * @param random where the jitter comes from
     * @return the points in the world; none when the ends are further apart than {@value #MAX_SPAN} blocks
     */
    public static List<Vec3> between(Shape shape, Vec3 from, Vec3 to, Moment moment, RandomGenerator random) {
        Vec3 line = to.minus(from);
        double length = line.length();
        if (length < 1e-6 || length > MAX_SPAN) return List.of();
        double reach = shape.grow() ? moment.progress() : 1;
        int count = (int) Math.clamp(Math.ceil(length * reach * shape.density()) + 1, 2, MAX_POINTS);
        Frame path = Frame.of(from, line);
        double angle = Math.toRadians(shape.twist() + shape.spin() * moment.emission());
        Vec3 lift = path.up().times(Math.cos(angle)).plus(path.right().times(Math.sin(angle)));
        Vec3 control = from.lerp(to, 0.5).plus(lift.times(2 * shape.bend()));
        List<Vec3> points = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            double share = reach * index / (count - 1);
            Vec3 point = shape.type() == Shape.ShapeType.ARC ? curve(from, control, to, share) : from.lerp(to, share);
            points.add(jitter(point, path, shape.jitter(), random));
        }
        return points;
    }

    /**
     * @return how many points the shape has, without drawing it; for limits and cost estimates
     */
    public static int count(Shape shape) {
        return switch (shape.type()) {
            case POINT -> 1;
            case RING, SPHERE, LINE -> shape.points();
            case SPIRAL -> Math.min(MAX_POINTS, shape.points() * shape.strands());
            case BEAM, ARC -> MAX_POINTS;
            case PATTERN -> Math.min(MAX_POINTS, cells(shape.rows()).size() * (shape.mirror() ? 2 : 1));
        };
    }

    /**
     * @return the cells of a picture that hold a point, as column and row, cut to {@value #MAX_CELLS} each way
     */
    public static List<int[]> cells(List<String> rows) {
        List<int[]> cells = new ArrayList<>();
        for (int row = 0; row < Math.min(rows.size(), MAX_CELLS); row++) {
            String line = rows.get(row);
            for (int column = 0; column < Math.min(line.length(), MAX_CELLS); column++) {
                if (line.charAt(column) != '.' && line.charAt(column) != ' ') cells.add(new int[] {column, row});
            }
        }
        return cells;
    }

    private static List<Vec3> line(Shape shape, Frame frame, double size) {
        int count = shape.points();
        List<Vec3> points = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            double share = count == 1 ? 0 : (double) index / (count - 1);
            points.add(frame.world(shape.to().times(share * size)));
        }
        return points;
    }

    private static List<Vec3> plane(Shape shape, Moment moment) {
        return switch (shape.type()) {
            case RING -> ring(shape.radius(), shape.points());
            case SPHERE -> sphere(shape.radius(), shape.points());
            case SPIRAL -> spiral(shape);
            case PATTERN -> pattern(shape, moment);
            case POINT, LINE, BEAM, ARC -> List.of(Vec3.ZERO);
        };
    }

    private static List<Vec3> ring(double radius, int count) {
        List<Vec3> points = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            double angle = 2 * Math.PI * index / count;
            points.add(new Vec3(radius * Math.cos(angle), radius * Math.sin(angle), 0));
        }
        return points;
    }

    private static List<Vec3> sphere(double radius, int count) {
        List<Vec3> points = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            double axis = 1 - 2 * (index + 0.5) / count;
            double across = Math.sqrt(1 - axis * axis);
            double angle = GOLDEN_ANGLE * index;
            points.add(new Vec3(across * Math.cos(angle), across * Math.sin(angle), axis).times(radius));
        }
        return points;
    }

    private static List<Vec3> spiral(Shape shape) {
        int strands = shape.strands();
        int perStrand = Math.max(1, Math.min(shape.points(), MAX_POINTS / strands));
        List<Vec3> points = new ArrayList<>(perStrand * strands);
        for (int strand = 0; strand < strands; strand++) {
            for (int index = 0; index < perStrand; index++) {
                double share = perStrand == 1 ? 0 : (double) index / (perStrand - 1);
                double angle = 2 * Math.PI * (shape.turns() * share + (double) strand / strands);
                points.add(new Vec3(
                        shape.radius() * Math.cos(angle), shape.radius() * Math.sin(angle), shape.height() * share));
            }
        }
        return points;
    }

    private static List<Vec3> pattern(Shape shape, Moment moment) {
        List<int[]> cells = cells(shape.rows());
        int width = cells.stream().mapToInt(cell -> cell[0]).max().orElse(0) + 1;
        int height = Math.min(shape.rows().size(), MAX_CELLS);
        Shape.Flap flap = shape.flap();
        double swing = flap == null
                ? 0
                : Math.toRadians(flap.degrees()) * Math.sin(2 * Math.PI * moment.tick() / flap.ticks());
        List<Vec3> points = new ArrayList<>();
        for (int[] cell : cells) {
            double up = ((height - 1) / 2.0 - cell[1]) * shape.scale();
            if (shape.mirror()) {
                double out = (cell[0] + 0.5) * shape.scale();
                points.add(fold(out, up, swing));
                points.add(fold(-out, up, swing));
            } else {
                points.add(fold((cell[0] - (width - 1) / 2.0) * shape.scale(), up, swing));
            }
            if (points.size() >= MAX_POINTS) break;
        }
        return points.size() > MAX_POINTS ? points.subList(0, MAX_POINTS) : points;
    }

    private static Vec3 fold(double across, double up, double swing) {
        double out = Math.abs(across);
        return new Vec3(Math.signum(across) * out * Math.cos(swing), up, -out * Math.sin(swing));
    }

    private static Vec3 turn(Vec3 plane, double angle) {
        if (angle == 0) return plane;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(plane.x() * cos - plane.y() * sin, plane.x() * sin + plane.y() * cos, plane.z());
    }

    private static Vec3 curve(Vec3 from, Vec3 control, Vec3 to, double share) {
        double rest = 1 - share;
        return from.times(rest * rest).plus(control.times(2 * rest * share)).plus(to.times(share * share));
    }

    private static Vec3 jitter(Vec3 point, Frame path, double jitter, RandomGenerator random) {
        if (jitter <= 0) return point;
        return point.plus(path.right().times(random.nextDouble(-jitter, jitter)))
                .plus(path.up().times(random.nextDouble(-jitter, jitter)));
    }
}
