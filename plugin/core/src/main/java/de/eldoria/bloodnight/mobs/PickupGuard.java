/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.loot.OrdinaryLoot;
import org.bukkit.World;
import org.bukkit.entity.Enemy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Keeps enemies from picking up items during a blood night whose loot is multiplied, so no item a player drops can
 * come back more than once.
 */
public final class PickupGuard implements Listener {
    private final Predicate<World> bloodNight;
    private final Function<World, OrdinaryLoot> loot;

    /**
     * @param bloodNight whether a blood night runs in a world
     * @param loot       the ordinary enemy loot settings of a world
     */
    public PickupGuard(Predicate<World> bloodNight, Function<World, OrdinaryLoot> loot) {
        this.bloodNight = bloodNight;
        this.loot = loot;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Enemy)) return;
        World world = event.getEntity().getWorld();
        OrdinaryLoot settings = loot.apply(world);
        if (settings.lootMultiplier() > 1 && bloodNight.test(world)) event.setCancelled(true);
    }
}
