/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime;

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
    CROSS;

    private static final double THIN = 0.06;
    private static final double LIFT = 0.02;
    private static final double BEAM = 0.25;

    /**
     * One box of a marker, relative to the marker's location: its size along each axis and the corner it starts at.
     */
    public record Bar(double width, double height, double depth, double x, double y, double z) {}

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
        };
    }
}
