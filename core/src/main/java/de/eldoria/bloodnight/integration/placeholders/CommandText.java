/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.placeholders;

import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Fills placeholders of other plugins into a command BloodNight runs, such as its begin and end commands.
 */
@FunctionalInterface
public interface CommandText {
    /**
     * Leaves commands as they are.
     */
    CommandText NONE = (command, player) -> command;

    /**
     * @param command the command
     * @param player  the player it runs for, if any
     * @return the command with placeholders filled in
     */
    String resolve(String command, @Nullable Player player);
}
