/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules.explosion;

import de.eldoria.bloodnight.configuration.world.WorldSettings;
import de.eldoria.bloodnight.configuration.world.mobsettings.CreeperSettings;
import de.eldoria.bloodnight.night.BloodNights;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.function.Function;

/**
 * Keeps creepers, special ones included, from breaking blocks during blood nights, or at all times in worlds that
 * ask for it. The explosion itself still happens.
 */
public final class CreeperGuard implements Listener {
    private final BloodNights nights;
    private final Function<World, WorldSettings> settings;

    /**
     * @param nights   the running blood nights
     * @param settings the settings of a world
     */
    public CreeperGuard(BloodNights nights, Function<World, WorldSettings> settings) {
        this.nights = nights;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Creeper)) return;
        World world = event.getEntity().getWorld();
        WorldSettings worldSettings = settings.apply(world);
        if (!worldSettings.enabled()) return;
        CreeperSettings creepers = worldSettings.mobSettings().creepers();
        if (creepers.blockDamage()) return;
        if (creepers.always() || nights.running(world).isPresent())
            event.blockList().clear();
    }
}
