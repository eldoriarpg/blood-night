/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.data;

import de.eldoria.bloodnight.particles.geometry.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * What a particle that takes data is drawn with. Which fields count depends on the particle's {@link DataKind}; the
 * others are ignored.
 *
 * @param color  colour as {@code #rrggbb}: dust, colour change (start), spell, plain colour, trail
 * @param to     the colour a colour change ends in
 * @param size   size of dust, 0.01 to 4
 * @param power  strength of a spell, dragon breath or sculk charge
 * @param delay  ticks before a shriek shows
 * @param block  block key for crumbling and falling blocks, e.g. {@code minecraft:redstone_block}
 * @param item   item id or material key for item bits
 * @param ticks  how long a trail flies
 * @param toward where a trail flies to, from the point it starts at, as x right, y up, z forward; without it, to the
 *               other end of the play, or a block up without one
 */
public record ParticleDataSpec(
        @Nullable String color,
        @Nullable String to,
        @Nullable Double size,
        @Nullable Double power,
        @Nullable Integer delay,
        @Nullable String block,
        @Nullable String item,
        @Nullable Integer ticks,
        @Nullable Vec3 toward) {
    /** No data at all. */
    public static final ParticleDataSpec NONE =
            new ParticleDataSpec(null, null, null, null, null, null, null, null, null);
}
