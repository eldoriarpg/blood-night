/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.bloodnight.mob.CustomMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A custom mob alive in a world, with the behaviour instance that holds its state.
 *
 * @param mob       the definition
 * @param entity    the mob
 * @param extension its extension, if any
 * @param behaviour its behaviour; does nothing when the mob's graph did not compile
 */
public record ActiveMob(
        CustomMob mob, LivingEntity entity, @Nullable Entity extension, CompiledBehaviour behaviour) {}
