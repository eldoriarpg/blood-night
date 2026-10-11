/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

import org.bukkit.entity.EntityType;

public record Extension(ExtensionType type, EntityType entityType, Equipment equipment) {}
