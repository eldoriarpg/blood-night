/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.particles.effect.ParticleEffect;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * The server's own particle effects by id, kept in {@code particle_effects.yml}. Built-in effects are not in this
 * file; see {@link EffectLibrary}.
 */
public class ParticleEffects {
    @JsonProperty
    private final Map<String, ParticleEffect> effects;

    @JsonCreator
    public ParticleEffects(@JsonProperty("effects") @Nullable Map<String, ParticleEffect> effects) {
        this.effects = new TreeMap<>(Objects.requireNonNullElse(effects, Map.of()));
    }

    public ParticleEffects() {
        this(null);
    }

    /**
     * @param id an effect's id
     * @return the effect, or null when there is none with that id
     */
    public @Nullable ParticleEffect get(String id) {
        return effects.get(id);
    }

    /**
     * @return every effect by id
     */
    public Map<String, ParticleEffect> effects() {
        return Collections.unmodifiableMap(effects);
    }
}
