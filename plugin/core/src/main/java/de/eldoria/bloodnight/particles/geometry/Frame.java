/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.geometry;

/**
 * Where a shape sits and how it is turned: an origin and three axes at right angles. Offsets and shape points are
 * given in this frame as x to the right, y up and z forward.
 *
 * @param origin  the frame's centre in the world
 * @param right   its x axis
 * @param up      its y axis
 * @param forward its z axis
 */
public record Frame(Vec3 origin, Vec3 right, Vec3 up, Vec3 forward) {
    /**
     * @param origin  the centre
     * @param forward where the frame faces; straight up or down faces north instead of turning over
     * @return a frame facing {@code forward} that stays upright
     */
    public static Frame of(Vec3 origin, Vec3 forward) {
        Vec3 ahead = forward.normalized(new Vec3(0, 0, 1));
        Vec3 hint = Math.abs(ahead.y()) > 0.999 ? new Vec3(0, 0, -Math.signum(ahead.y())) : Vec3.UP;
        Vec3 right = ahead.cross(hint).normalized(new Vec3(-1, 0, 0));
        Vec3 up = right.cross(ahead).normalized(Vec3.UP);
        return new Frame(origin, right, up, ahead);
    }

    /**
     * @param local a point in this frame
     * @return the point in the world
     */
    public Vec3 world(Vec3 local) {
        return origin.plus(right.times(local.x())).plus(up.times(local.y())).plus(forward.times(local.z()));
    }

    /**
     * @return the same axes around another origin
     */
    public Frame at(Vec3 origin) {
        return new Frame(origin, right, up, forward);
    }
}
