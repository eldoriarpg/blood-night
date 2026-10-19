/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.effect;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Where an emitter or a piece sits: a point on one end of the play, or on one of the effect's pieces. At a plain
 * location, every body point is the location itself.
 *
 * @param at    the point on the entity's body
 * @param on    which end, or a piece
 * @param piece the piece's index, for {@link EffectEnd#PIECE}
 * @param aim   the point on the other end's body it aims at, e.g. a beam from the eyes to the chest; none aims at
 *              the same point as {@code at}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Anchor(
        BodyPoint at, EffectEnd on, Integer piece, @Nullable BodyPoint aim) {
    /** The centre of the source. */
    public static final Anchor SOURCE_CENTER = new Anchor(BodyPoint.CENTER, EffectEnd.SOURCE, 0, null);

    /** A point on an entity's body. */
    public enum BodyPoint {
        /** Where it stands. */
        FEET,
        /** Half its height up. */
        CENTER,
        /** Its eyes. */
        EYES,
        /** A little above its head. */
        ABOVE
    }

    /** What an anchor sits on. */
    public enum EffectEnd {
        /** The entity or place the effect plays at. */
        SOURCE,
        /** The other end, if the play has one. */
        TARGET,
        /** One of the effect's display pieces, so particles follow it. */
        PIECE
    }

    public Anchor {
        at = Objects.requireNonNullElse(at, BodyPoint.CENTER);
        on = Objects.requireNonNullElse(on, EffectEnd.SOURCE);
        piece = Math.max(0, Objects.requireNonNullElse(piece, 0));
    }

    /**
     * @return the point on the other end's body it aims at
     */
    public BodyPoint aimed() {
        return aim == null ? at : aim;
    }
}
