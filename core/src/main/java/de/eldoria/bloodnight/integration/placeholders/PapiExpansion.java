/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * BloodNight's placeholders for PlaceholderAPI: {@code %bloodnight_<name>[_<arguments>]%}, see {@link Placeholders}.
 * Only loaded when PlaceholderAPI runs.
 */
public final class PapiExpansion extends PlaceholderExpansion {
    private final Placeholders placeholders;
    private final String version;

    /**
     * @param placeholders the values
     * @param version      the plugin's version
     */
    public PapiExpansion(Placeholders placeholders, String version) {
        this.placeholders = placeholders;
        this.version = version;
    }

    @Override
    public String getIdentifier() {
        return "bloodnight";
    }

    @Override
    public String getAuthor() {
        return "RainbowDashLabs";
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer player, String parameter) {
        Placeholders.Request request = Placeholders.parse(parameter);
        if (request == null) return null;
        return placeholders.value(request, player instanceof Player online ? online.getWorld() : null);
    }
}
