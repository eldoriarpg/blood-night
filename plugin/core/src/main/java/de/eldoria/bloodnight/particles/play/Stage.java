/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.play;

import de.eldoria.bloodnight.configuration.server.ParticleSettings;
import de.eldoria.bloodnight.tasks.Tasks;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * What every play needs from the plugin.
 *
 * @param tasks    runs work on the owning threads
 * @param viewers  who sees effects
 * @param budget   how many particles may still be sent this tick
 * @param settings the current particle settings
 * @param items    the item registry's items by id
 */
public record Stage(
        Tasks tasks,
        Viewers viewers,
        Budget budget,
        Supplier<ParticleSettings> settings,
        Function<String, @Nullable ItemStack> items) {}
