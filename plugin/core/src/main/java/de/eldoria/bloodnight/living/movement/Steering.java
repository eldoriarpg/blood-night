/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.living.movement;

import de.eldoria.bloodnight.mob.attributes.Movement;
import org.bukkit.entity.LivingEntity;

/**
 * Gives custom mobs their movement. Runs on the mob's thread.
 */
public interface Steering {
    /**
     * Leaves every mob to its vanilla AI.
     */
    Steering NONE = (entity, movement) -> {};

    /**
     * Replaces the movement a mob had with another; {@code VANILLA} takes it away again.
     *
     * @param entity   the mob
     * @param movement its movement
     */
    void steer(LivingEntity entity, Movement movement);
}
