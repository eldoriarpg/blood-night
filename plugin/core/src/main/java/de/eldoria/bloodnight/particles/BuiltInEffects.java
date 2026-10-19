/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles;

import de.eldoria.bloodnight.particles.effect.ParticleEffect;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * The particle effects shipped in the jar ({@code particles/built_in.yml}), read only, with ids under
 * {@value #NAMESPACE}. Plugin updates improve them on every server; the editor copies one to change it.
 */
public final class BuiltInEffects {
    /** The namespace of every built-in effect's id. */
    public static final String NAMESPACE = "bloodnight:";

    private static final String RESOURCE = "particles/built_in.yml";
    private static final YAMLMapper YAML = YAMLMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    private static volatile Map<String, ParticleEffect> effects;

    private BuiltInEffects() {}

    /**
     * @return every built-in effect by its full id, e.g. {@code bloodnight:blood_aura}
     */
    public static Map<String, ParticleEffect> all() {
        Map<String, ParticleEffect> loaded = effects;
        if (loaded == null) {
            loaded = read();
            effects = loaded;
        }
        return loaded;
    }

    /**
     * @return whether the id is a built-in effect's, which cannot be changed
     */
    public static boolean builtIn(String id) {
        return id.startsWith(NAMESPACE);
    }

    private static Map<String, ParticleEffect> read() {
        try (InputStream in =
                Objects.requireNonNull(BuiltInEffects.class.getClassLoader().getResourceAsStream(RESOURCE), RESOURCE)) {
            ParticleEffects file = YAML.readValue(in, ParticleEffects.class);
            Map<String, ParticleEffect> byId = new TreeMap<>();
            file.effects().forEach((id, effect) -> byId.put(NAMESPACE + id, effect));
            return Collections.unmodifiableMap(byId);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
