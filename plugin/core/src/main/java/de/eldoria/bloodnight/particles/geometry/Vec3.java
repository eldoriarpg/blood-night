/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.geometry;

/**
 * A point or a direction in blocks, free of Bukkit so effects can be worked out and tested anywhere.
 *
 * @param x east and west, or right and left in a frame
 * @param y up and down
 * @param z south and north, or forward and back in a frame
 */
public record Vec3(double x, double y, double z) {
    /** No offset at all. */
    public static final Vec3 ZERO = new Vec3(0, 0, 0);

    /** Straight up. */
    public static final Vec3 UP = new Vec3(0, 1, 0);

    /**
     * @return this plus the other
     */
    public Vec3 plus(Vec3 other) {
        return new Vec3(x + other.x, y + other.y, z + other.z);
    }

    /**
     * @return this minus the other
     */
    public Vec3 minus(Vec3 other) {
        return new Vec3(x - other.x, y - other.y, z - other.z);
    }

    /**
     * @return this scaled by a factor
     */
    public Vec3 times(double factor) {
        return new Vec3(x * factor, y * factor, z * factor);
    }

    /**
     * @return the dot product
     */
    public double dot(Vec3 other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /**
     * @return the cross product
     */
    public Vec3 cross(Vec3 other) {
        return new Vec3(y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x);
    }

    /**
     * @return the length
     */
    public double length() {
        return Math.sqrt(dot(this));
    }

    /**
     * @return this with length 1, or {@code fallback} when it has no length
     */
    public Vec3 normalized(Vec3 fallback) {
        double length = length();
        return length < 1e-9 ? fallback : times(1 / length);
    }

    /**
     * @return the point a share of the way from this to the other
     */
    public Vec3 lerp(Vec3 other, double share) {
        return plus(other.minus(this).times(share));
    }

    /**
     * @param yawDegrees  Minecraft yaw: 0 faces south, 90 west
     * @param pitchDegrees Minecraft pitch: negative looks up
     * @return the direction a body or eye with this yaw and pitch faces
     */
    public static Vec3 facing(double yawDegrees, double pitchDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);
        double horizontal = Math.cos(pitch);
        return new Vec3(-Math.sin(yaw) * horizontal, -Math.sin(pitch), Math.cos(yaw) * horizontal);
    }
}
