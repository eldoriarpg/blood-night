/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles;

import de.eldoria.bloodnight.particles.effect.ParticleEffect;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Every particle effect a graph or a mob can name: the built-in ones and the server's own, read again on every
 * lookup so a reload takes effect without recompiling anything.
 */
public final class EffectLibrary {
    private final Supplier<ParticleEffects> own;

    /**
     * @param own the server's own effects, as currently loaded
     */
    public EffectLibrary(Supplier<ParticleEffects> own) {
        this.own = own;
    }

    /**
     * @param id a built-in effect's id ({@code bloodnight:...}) or one of the server's
     * @return the effect, or {@code null} when no effect has that id
     */
    public @Nullable ParticleEffect get(String id) {
        if (BuiltInEffects.builtIn(id)) return BuiltInEffects.all().get(id);
        return own.get().get(id);
    }

    /**
     * @return whether an effect has the id
     */
    public boolean knows(String id) {
        return get(id) != null;
    }

    /**
     * @return every effect by id, built-in ones first
     */
    public Map<String, ParticleEffect> all() {
        Map<String, ParticleEffect> all = new TreeMap<>(BuiltInEffects.all());
        all.putAll(own.get().effects());
        return all;
    }
}
