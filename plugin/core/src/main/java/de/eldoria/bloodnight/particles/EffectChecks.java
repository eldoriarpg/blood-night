/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles;

import de.eldoria.bloodnight.particles.data.DataKind;
import de.eldoria.bloodnight.particles.data.ParticleDatas;
import de.eldoria.bloodnight.particles.effect.Anchor;
import de.eldoria.bloodnight.particles.effect.Emitter;
import de.eldoria.bloodnight.particles.effect.ParticleEffect;
import de.eldoria.bloodnight.particles.effect.Piece;
import de.eldoria.bloodnight.particles.geometry.Shape;
import de.eldoria.bloodnight.particles.geometry.Shapes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/**
 * Says what is wrong with a particle effect before it is applied: too many parts, shapes with too many particles,
 * pictures too large, unknown particles, data that does not fit, pieces without what they show. Values out of range
 * are not reported; they are clamped when the effect is read.
 * <p>
 * Particles are looked up in the running server's registry; without a server they are not checked.
 */
public final class EffectChecks {
    private EffectChecks() {}

    /**
     * @param id     the effect's id, for the messages
     * @param effect the effect
     * @param items  whether an item id or material key is known
     * @return the problems, empty when the effect is fine
     */
    public static List<String> problems(String id, ParticleEffect effect, Predicate<String> items) {
        List<String> problems = new ArrayList<>();
        if (effect.emitters().size() > ParticleEffect.MAX_EMITTERS) {
            problems.add("Particle effect %s has more than %d emitters".formatted(id, ParticleEffect.MAX_EMITTERS));
        }
        if (effect.pieces().size() > ParticleEffect.MAX_PIECES) {
            problems.add("Particle effect %s has more than %d pieces".formatted(id, ParticleEffect.MAX_PIECES));
        }
        for (int index = 0; index < effect.emitters().size(); index++) {
            String where = "Particle effect %s, emitter %d".formatted(id, index + 1);
            emitter(where, effect.emitters().get(index), effect.pieces().size(), items)
                    .forEach(problems::add);
        }
        for (int index = 0; index < effect.pieces().size(); index++) {
            String where = "Particle effect %s, piece %d".formatted(id, index + 1);
            piece(where, effect.pieces().get(index), items).forEach(problems::add);
        }
        return problems;
    }

    private static List<String> emitter(String where, Emitter emitter, int pieces, Predicate<String> items) {
        List<String> problems = new ArrayList<>();
        Shape shape = emitter.shape();
        int particles = Shapes.count(shape) * emitter.count();
        if (!shape.betweenEnds() && particles > ParticleEffect.MAX_PER_EMISSION) {
            problems.add("%s draws %d particles at once, at most %d"
                    .formatted(where, particles, ParticleEffect.MAX_PER_EMISSION));
        }
        if (shape.type() == Shape.ShapeType.PATTERN) pattern(where, shape).forEach(problems::add);
        if (emitter.anchor().on() == Anchor.EffectEnd.PIECE && emitter.anchor().piece() >= pieces) {
            problems.add("%s sits on piece %d, which the effect does not have"
                    .formatted(where, emitter.anchor().piece() + 1));
        }
        Particle particle = particle(emitter.particle());
        if (Bukkit.getServer() != null && particle == null) {
            problems.add("%s uses unknown particle %s".formatted(where, emitter.particle()));
        } else if (particle != null) {
            ParticleDatas.problems(DataKind.of(particle), emitter.data(), items::test)
                    .forEach(problem -> problems.add("%s: %s %s".formatted(where, emitter.particle(), problem)));
        }
        return problems;
    }

    private static List<String> pattern(String where, Shape shape) {
        List<String> problems = new ArrayList<>();
        if (shape.rows().size() > Shapes.MAX_CELLS
                || shape.rows().stream().anyMatch(row -> row.length() > Shapes.MAX_CELLS)) {
            problems.add("%s has a picture larger than %d x %d".formatted(where, Shapes.MAX_CELLS, Shapes.MAX_CELLS));
        }
        int points = Shapes.cells(shape.rows()).size() * (shape.mirror() ? 2 : 1);
        if (points > Shapes.MAX_POINTS) {
            problems.add("%s has a picture of %d points, at most %d".formatted(where, points, Shapes.MAX_POINTS));
        }
        if (points == 0) problems.add("%s has an empty picture".formatted(where));
        return problems;
    }

    private static List<String> piece(String where, Piece piece, Predicate<String> items) {
        List<String> problems = new ArrayList<>();
        if (piece.anchor().on() == Anchor.EffectEnd.PIECE) problems.add("%s cannot sit on a piece".formatted(where));
        if (piece.kind() == Piece.PieceKind.ITEM) {
            if (piece.item() == null || !items.test(piece.item())) {
                problems.add("%s shows unknown item %s".formatted(where, piece.item()));
            }
        } else if (block(piece.block()) == null) {
            problems.add("%s shows unknown block %s".formatted(where, piece.block()));
        }
        return problems;
    }

    private static @Nullable Material block(@Nullable String key) {
        if (key == null) return null;
        Material material = Material.matchMaterial(key.toLowerCase(Locale.ROOT));
        return material != null && material.isBlock() ? material : null;
    }

    /**
     * @return the particle with that key, or {@code null} when there is no server or no such particle
     */
    public static @Nullable Particle particle(String key) {
        if (Bukkit.getServer() == null) return null;
        NamespacedKey namespaced = NamespacedKey.fromString(key.toLowerCase(Locale.ROOT));
        if (namespaced == null) return null;
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.PARTICLE_TYPE)
                .get(namespaced);
    }
}
