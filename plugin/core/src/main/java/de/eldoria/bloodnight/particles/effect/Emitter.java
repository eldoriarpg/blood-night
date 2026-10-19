/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.effect;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.eldoria.bloodnight.particles.data.ParticleDataSpec;
import de.eldoria.bloodnight.particles.geometry.Shape;
import de.eldoria.bloodnight.particles.geometry.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * One particle drawn in a shape, again and again while its effect plays. A missing field takes its default.
 *
 * @param particle the particle's key, e.g. {@code minecraft:dust}
 * @param data     what the particle is drawn with, for particles that take data
 * @param anchor   where it sits
 * @param offset   shift from the anchor in blocks, as x right, y up, z forward of the shape's facing
 * @param shape    where its particles appear around the anchor
 * @param motion   movement from its end to the other one; none stays at the anchor
 * @param count    particles at every point of the shape
 * @param spread   random box around each point, in blocks each way
 * @param speed    how fast the particles fly off, the meaning depends on the particle
 * @param every    ticks between two emissions, at least 1
 * @param delay    ticks into the play before the first emission
 * @param chance   share of emissions that happen, 0 to 1, for flicker
 * @param tiles    emissions the shape is built over: each draws every {@code tiles}th point, the next one the points
 *                 after, so a shape of long-lived particles stays whole at a fraction of the particles
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Emitter(
        String particle,
        ParticleDataSpec data,
        Anchor anchor,
        Vec3 offset,
        Shape shape,
        @Nullable Motion motion,
        Integer count,
        Vec3 spread,
        Double speed,
        Integer every,
        Integer delay,
        Double chance,
        Integer tiles) {
    /** The most emissions a shape is built over. */
    public static final int MAX_TILES = 20;

    public Emitter {
        particle = Objects.requireNonNullElse(particle, "minecraft:flame");
        data = Objects.requireNonNullElse(data, ParticleDataSpec.NONE);
        anchor = Objects.requireNonNullElse(anchor, Anchor.SOURCE_CENTER);
        offset = Objects.requireNonNullElse(offset, Vec3.ZERO);
        shape = Objects.requireNonNullElse(shape, Shape.POINT);
        count = Math.clamp(Objects.requireNonNullElse(count, 1), 1, ParticleEffect.MAX_PER_EMISSION);
        spread = Objects.requireNonNullElse(spread, Vec3.ZERO);
        speed = Math.max(0, Objects.requireNonNullElse(speed, 0.0));
        every = Math.clamp(Objects.requireNonNullElse(every, 1), 1, ParticleEffect.MAX_DURATION);
        delay = Math.clamp(Objects.requireNonNullElse(delay, 0), 0, ParticleEffect.MAX_DURATION);
        chance = Math.clamp(Objects.requireNonNullElse(chance, 1.0), 0, 1);
        tiles = Math.clamp(Objects.requireNonNullElse(tiles, 1), 1, MAX_TILES);
    }

    /**
     * @param particle the particle's key
     * @param shape    where it appears
     * @return an emitter with every other field at its default
     */
    public static Emitter of(String particle, Shape shape) {
        return new Emitter(
                particle,
                ParticleDataSpec.NONE,
                Anchor.SOURCE_CENTER,
                Vec3.ZERO,
                shape,
                null,
                1,
                Vec3.ZERO,
                0.0,
                1,
                0,
                1.0,
                1);
    }
}
