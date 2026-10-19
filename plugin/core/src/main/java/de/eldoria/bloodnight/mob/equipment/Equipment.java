/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.equipment;

import org.jspecify.annotations.Nullable;

public record Equipment(
        @Nullable String head,
        @Nullable String chest,
        @Nullable String legs,
        @Nullable String feet,
        @Nullable String mainHand,
        @Nullable String offHand) {}
