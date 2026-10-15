/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;

import java.util.Arrays;
import java.util.List;

/**
 * The entity types a custom mob or its extension can be: spawnable mobs, which move, target and fight. Projectiles,
 * vehicles, armor stands and the like are none.
 */
public final class MobTypes {
    private MobTypes() {}

    /**
     * @param type an entity type
     * @return whether a custom mob can be one
     */
    public static boolean isMob(EntityType type) {
        Class<?> entityClass = type.getEntityClass();
        return type.isSpawnable() && entityClass != null && Mob.class.isAssignableFrom(entityClass);
    }

    /**
     * @return every entity type a custom mob can be, by name
     */
    public static List<String> all() {
        return Arrays.stream(EntityType.values())
                .filter(MobTypes::isMob)
                .map(EntityType::name)
                .sorted()
                .toList();
    }
}
