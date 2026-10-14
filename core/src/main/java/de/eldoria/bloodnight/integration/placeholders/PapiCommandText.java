/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.placeholders;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Fills PlaceholderAPI placeholders into commands. Only loaded when PlaceholderAPI runs.
 */
public final class PapiCommandText implements CommandText {
    @Override
    public String resolve(String command, @Nullable Player player) {
        return PlaceholderAPI.setPlaceholders((OfflinePlayer) player, command);
    }
}
