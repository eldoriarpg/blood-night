/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api;

import org.bukkit.Bukkit;

/**
 * Finds the {@link BloodNightService} from anywhere.
 */
public final class BloodNightProvider {
    private BloodNightProvider() {}

    /**
     * @return the service
     * @throws IllegalStateException when BloodNight is not enabled
     */
    public static BloodNightService get() {
        BloodNightService service = Bukkit.getServicesManager().load(BloodNightService.class);
        if (service == null) throw new IllegalStateException("BloodNight is not enabled");
        return service;
    }
}
