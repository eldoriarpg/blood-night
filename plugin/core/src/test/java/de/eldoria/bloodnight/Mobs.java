/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight;

import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.attributes.Attributes;
import de.eldoria.bloodnight.mob.attributes.MobName;
import de.eldoria.bloodnight.mob.attributes.ValueModifier;
import de.eldoria.bloodnight.mob.equipment.Equipment;
import de.eldoria.bloodnight.mob.equipment.Extension;
import de.eldoria.bloodnight.mob.equipment.ExtensionType;
import de.eldoria.bloodnight.mob.spawn.SpawnRules;
import de.eldoria.graph.model.Graph;
import org.bukkit.entity.EntityType;

/**
 * Mob definitions for tests.
 */
public final class Mobs {
    private Mobs() {}

    public static CustomMob zombie(String id, Graph behaviour) {
        return new CustomMob(
                id,
                new Equipment(null, null, null, null, null, null),
                new Attributes(
                        MobName.english(id),
                        EntityType.ZOMBIE,
                        false,
                        ValueModifier.DEFAULT,
                        20,
                        ValueModifier.DEFAULT,
                        1),
                behaviour,
                null,
                new Extension(ExtensionType.NONE, null, null),
                SpawnRules.defaults());
    }
}
