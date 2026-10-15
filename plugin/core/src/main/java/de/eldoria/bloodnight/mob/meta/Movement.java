/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.api.MovementPreset;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * How a custom mob moves, and the numbers its preset reads. Which numbers a preset uses:
 * <ul>
 *   <li>{@code range}: how far {@code AGGRESSIVE} hunts, {@code PACK} looks for its kind, {@code GUARD} strays</li>
 *   <li>{@code closest} and {@code farthest}: the distance {@code KEEP_DISTANCE} and {@code STALKER} keep</li>
 *   <li>{@code seconds}: how long {@code HIT_AND_RUN} backs off after a hit</li>
 *   <li>{@code fleeBelow}: the share of its health below which {@code COWARD} flees; it comes back at twice that</li>
 * </ul>
 *
 * @param preset    the movement
 * @param range     blocks
 * @param closest   blocks
 * @param farthest  blocks
 * @param seconds   seconds
 * @param fleeBelow from 0 to 1
 */
public record Movement(
        MovementPreset preset, double range, double closest, double farthest, double seconds, double fleeBelow) {
    /**
     * The vanilla movement with the default numbers.
     */
    public static final Movement VANILLA = of(MovementPreset.VANILLA);

    public Movement {
        preset = Objects.requireNonNullElse(preset, MovementPreset.VANILLA);
        range = Math.max(range, 1);
        closest = Math.max(closest, 0);
        farthest = Math.max(farthest, closest);
        seconds = Math.max(seconds, 0);
        fleeBelow = Math.clamp(fleeBelow, 0, 1);
    }

    /**
     * @param preset the movement
     * @return the movement with the default numbers: range 24, closest 6, farthest 10, 3 seconds, flees below 30 %
     */
    public static Movement of(MovementPreset preset) {
        return new Movement(preset, 24, 6, 10, 3, 0.3);
    }

    /**
     * Reads a movement; numbers it leaves out take their defaults.
     */
    @JsonCreator
    static Movement read(
            @JsonProperty("preset") @Nullable MovementPreset preset,
            @JsonProperty("range") @Nullable Double range,
            @JsonProperty("closest") @Nullable Double closest,
            @JsonProperty("farthest") @Nullable Double farthest,
            @JsonProperty("seconds") @Nullable Double seconds,
            @JsonProperty("fleeBelow") @Nullable Double fleeBelow) {
        Movement defaults = of(Objects.requireNonNullElse(preset, MovementPreset.VANILLA));
        return new Movement(
                defaults.preset(),
                Objects.requireNonNullElse(range, defaults.range()),
                Objects.requireNonNullElse(closest, defaults.closest()),
                Objects.requireNonNullElse(farthest, defaults.farthest()),
                Objects.requireNonNullElse(seconds, defaults.seconds()),
                Objects.requireNonNullElse(fleeBelow, defaults.fleeBelow()));
    }

    /**
     * @param other another movement
     * @return this movement's numbers with the other preset
     */
    public Movement with(MovementPreset other) {
        return new Movement(other, range, closest, farthest, seconds, fleeBelow);
    }

    /**
     * @param movement a movement as read, may be missing
     * @return it, or {@link #VANILLA}
     */
    public static Movement orVanilla(@Nullable Movement movement) {
        return movement == null ? VANILLA : movement;
    }
}
