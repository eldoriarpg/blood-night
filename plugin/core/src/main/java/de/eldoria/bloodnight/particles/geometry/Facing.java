/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.geometry;

/**
 * How a shape or a display piece is turned. Flat shapes (rings, spirals, pictures) lie on the ground with {@link #UP}
 * and stand across the facing direction with every other value, so a ring facing {@link #PATH} is a disc the line
 * from source to target passes through.
 */
public enum Facing {
    /** Flat on the ground, turned with the body. */
    UP,
    /** Turned the way the entity looks, up and down included, e.g. a halo behind the head. */
    LOOK,
    /** Upright, turned with the body, so a shape stays on its back as it turns, e.g. wings. */
    BODY,
    /** Along the line from source to target. */
    PATH
}
