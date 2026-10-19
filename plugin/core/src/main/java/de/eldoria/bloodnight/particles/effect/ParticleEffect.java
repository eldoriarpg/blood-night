/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.effect;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Objects;

/**
 * A named, reusable particle effect: emitters drawing particles in shapes, and display pieces, playing at a place,
 * on an entity, or between a source and a target. Graphs play it by id, and mobs wear it.
 *
 * @param name     the name shown in the editor
 * @param duration ticks one play lasts, 0 for a single tick, at most {@value #MAX_DURATION}
 * @param emitters what draws particles, at most {@value #MAX_EMITTERS}
 * @param pieces   display pieces, at most {@value #MAX_PIECES}
 * @param self     whether a player the effect plays on sees it too; wings right behind the camera would block
 *                 the view
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ParticleEffect(String name, Integer duration, List<Emitter> emitters, List<Piece> pieces, Boolean self) {
    /** The longest play, in ticks. */
    public static final int MAX_DURATION = 600;

    /** The most emitters one effect has. */
    public static final int MAX_EMITTERS = 8;

    /** The most pieces one effect has. */
    public static final int MAX_PIECES = 4;

    /** The most particles one emission of one emitter draws, points times count. */
    public static final int MAX_PER_EMISSION = 256;

    public ParticleEffect {
        name = Objects.requireNonNullElse(name, "");
        duration = Math.clamp(Objects.requireNonNullElse(duration, 20), 0, MAX_DURATION);
        emitters = List.copyOf(Objects.requireNonNullElse(emitters, List.of()));
        pieces = List.copyOf(Objects.requireNonNullElse(pieces, List.of()));
        self = Objects.requireNonNullElse(self, true);
    }
}
