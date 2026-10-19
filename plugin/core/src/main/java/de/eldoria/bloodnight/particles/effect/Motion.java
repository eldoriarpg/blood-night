/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.effect;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.eldoria.bloodnight.particles.geometry.Vec3;

import java.util.Objects;

/**
 * Movement of an emitter or a piece from its own end to the other one: it reaches the other end after {@code ticks}
 * and goes on {@code through} blocks further along the same line at the same speed, then stays there.
 *
 * @param type    the kind of movement
 * @param ticks   how long it takes to reach the other end, at least 1
 * @param through how far it goes on past the other end, in blocks
 * @param ease    how it speeds up or slows down on the way
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Motion(MotionType type, Integer ticks, Double through, Ease ease) {
    /** The kinds of movement. */
    public enum MotionType {
        /** From its own end to the other one. */
        TRAVEL
    }

    /** How a movement speeds up or slows down. */
    public enum Ease {
        /** The same speed all the way. */
        LINEAR,
        /** Starts slow, ends fast, like a thrown spear. */
        IN,
        /** Starts fast, ends slow. */
        OUT
    }

    public Motion {
        type = Objects.requireNonNullElse(type, MotionType.TRAVEL);
        ticks = Math.clamp(Objects.requireNonNullElse(ticks, 10), 1, 600);
        through = Math.max(0, Objects.requireNonNullElse(through, 0.0));
        ease = Objects.requireNonNullElse(ease, Ease.LINEAR);
    }

    /**
     * @return the ticks until the movement stops, the way past the other end included
     */
    public long length(double distance) {
        if (distance <= 0) return ticks;
        return ticks + Math.round(Math.ceil(ticks * through / distance));
    }

    /**
     * @param from    where it starts
     * @param to      the other end
     * @param elapsed ticks since it started
     * @return where it is
     */
    public Vec3 position(Vec3 from, Vec3 to, double elapsed) {
        double distance = to.minus(from).length();
        if (distance < 1e-6) return to;
        double share = elapsed / ticks;
        double furthest = 1 + through / distance;
        double travelled = share <= 1 ? eased(Math.max(share, 0)) : Math.min(share, furthest);
        return from.lerp(to, travelled);
    }

    private double eased(double share) {
        return switch (ease) {
            case LINEAR -> share;
            case IN -> share * share;
            case OUT -> 1 - (1 - share) * (1 - share);
        };
    }
}
