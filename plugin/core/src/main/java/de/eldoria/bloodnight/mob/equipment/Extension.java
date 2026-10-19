/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.equipment;

import org.bukkit.entity.EntityType;
import org.jspecify.annotations.Nullable;

/**
 * An entity spawned together with the mob, riding or carrying it.
 *
 * @param type       how the extension is attached; {@link ExtensionType#NONE} for none
 * @param entityType type of the extension; none when there is no extension
 * @param equipment  item ids per slot of the extension; none keeps what it spawned with
 */
public record Extension(
        ExtensionType type,
        @Nullable EntityType entityType,
        @Nullable Equipment equipment) {}
