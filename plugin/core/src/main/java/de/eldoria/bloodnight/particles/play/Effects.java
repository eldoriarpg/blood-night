/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.play;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Plays particle effects for mobs: those graphs play, and those mobs wear while they live, spawn and die.
 */
public interface Effects {
    /** Plays nothing; for tests and setups without effects. */
    Effects NONE = new Effects() {
        @Override
        public long play(
                String id, Object source, @Nullable Object target, @Nullable Player viewer, @Nullable UUID owner) {
            return -1;
        }

        @Override
        public void spawned(LivingEntity mob, @Nullable String id) {}

        @Override
        public void ticked(LivingEntity mob, @Nullable String ambient) {}

        @Override
        public void died(LivingEntity mob, @Nullable String id) {}

        @Override
        public void forget(UUID mob) {}
    };

    /**
     * Plays an effect once.
     *
     * @param id     the effect's id
     * @param source an entity, followed, or a location
     * @param target the other end, an entity or a location, or {@code null}
     * @param viewer the only player who sees it, or {@code null} for everyone near
     * @param owner  the mob playing it, whose limits it counts against, or {@code null}
     * @return ticks until what travels in it reaches the target, 0 when nothing travels; -1 when it does not play
     */
    long play(String id, Object source, @Nullable Object target, @Nullable Player viewer, @Nullable UUID owner);

    /**
     * A mob spawned; plays its spawn effect. Runs on the mob's thread.
     */
    void spawned(LivingEntity mob, @Nullable String id);

    /**
     * A mob ticked; plays the effect it wears, starting it again when it ends. Runs on the mob's thread.
     */
    void ticked(LivingEntity mob, @Nullable String ambient);

    /**
     * A mob died; plays its death effect where it died.
     */
    void died(LivingEntity mob, @Nullable String id);

    /**
     * A mob is gone; drops what is kept for it.
     */
    void forget(UUID mob);
}
