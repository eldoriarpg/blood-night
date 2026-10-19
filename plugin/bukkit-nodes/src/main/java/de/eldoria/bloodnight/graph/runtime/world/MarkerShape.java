/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime.world;

import java.util.ArrayList;
import java.util.List;

/**
 * The shapes a warning marker takes, each made of flat or upright bars of coloured glass.
 */
public enum MarkerShape {
    /**
     * A flat square on the ground; its size is the radius.
     */
    AREA,
    /**
     * A thin column of light; its size is the height.
     */
    PILLAR,
    /**
     * Two flat bars crossing on the ground; its size is the radius.
     */
    CROSS,
    /**
     * A flat ring on the ground, made of short bars turned along its edge; its size is the radius.
     */
    CIRCLE;

    private static final double THIN = 0.06;
    private static final double LIFT = 0.02;
    private static final double BEAM = 0.25;
    private static final double SEGMENT = 0.6;
    private static final int FEWEST_SEGMENTS = 16;
    private static final int MOST_SEGMENTS = 48;
    private static final double OVERLAP = 1.05;

    /**
     * One box of a marker, relative to the marker's location: its size along each axis, the corner it starts at, and
     * how far it is turned about that corner around the vertical axis.
     *
     * @param yaw the turn in radians; a turn of a quarter maps the box's width onto its negative z axis
     */
    public record Bar(double width, double height, double depth, double x, double y, double z, double yaw) {
        /**
         * A box that is not turned.
         */
        public Bar(double width, double height, double depth, double x, double y, double z) {
            this(width, height, depth, x, y, z, 0);
        }
    }

    /**
     * @param size the marker's size, at least a tenth of a block
     * @return the boxes the shape is made of
     */
    public List<Bar> bars(double size) {
        double s = Math.max(size, 0.1);
        return switch (this) {
            case AREA -> List.of(new Bar(2 * s, THIN, 2 * s, -s, LIFT, -s));
            case PILLAR -> List.of(new Bar(BEAM, s, BEAM, -BEAM / 2, 0, -BEAM / 2));
            case CROSS ->
                List.of(
                        new Bar(2 * s, THIN, BEAM, -s, LIFT, -BEAM / 2),
                        new Bar(BEAM, THIN, 2 * s, -BEAM / 2, LIFT, -s));
            case CIRCLE -> ring(s);
        };
    }

    /**
     * Short bars along a circle, each turned so its length follows the edge and centred on it; neighbours overlap a
     * little so the ring shows no gaps. Larger rings get more bars, between {@value #FEWEST_SEGMENTS} and
     * {@value #MOST_SEGMENTS}.
     */
    private static List<Bar> ring(double radius) {
        int count = (int) Math.clamp(Math.round(2 * Math.PI * radius / SEGMENT), FEWEST_SEGMENTS, MOST_SEGMENTS);
        double length = 2 * Math.PI * radius / count * OVERLAP;
        List<Bar> bars = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double yaw = -angle - Math.PI / 2;
            double centreX = length / 2 * Math.cos(yaw) + BEAM / 2 * Math.sin(yaw);
            double centreZ = -length / 2 * Math.sin(yaw) + BEAM / 2 * Math.cos(yaw);
            bars.add(new Bar(
                    length,
                    THIN,
                    BEAM,
                    radius * Math.cos(angle) - centreX,
                    LIFT,
                    radius * Math.sin(angle) - centreZ,
                    yaw));
        }
        return bars;
    }
}
