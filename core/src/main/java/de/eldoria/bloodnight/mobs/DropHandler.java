/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.configuration.elements.GlobalDrops;
import de.eldoria.bloodnight.configuration.elements.world.WorldDrops;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * Adds the configured drops of a custom mob killed by a player to its vanilla loot and changes the experience it
 * drops (see {@link DropRules}). Deaths without a player behind them, such as sunlight or lava, keep their vanilla
 * drops only. Unknown item ids are left out.
 */
public final class DropHandler implements Listener {
    private final Function<String, @Nullable CustomMob> mobs;
    private final Function<World, WorldDrops> worlds;
    private final Supplier<GlobalDrops> global;
    private final Function<String, @Nullable ItemStack> items;
    private final RandomGenerator random;

    /**
     * @param mobs   the custom mob of an id, if defined
     * @param worlds the drop settings of a world
     * @param global the global drop settings
     * @param items  a new item of an item id or material key, if known
     * @param random the source of chance
     */
    public DropHandler(
            Function<String, @Nullable CustomMob> mobs,
            Function<World, WorldDrops> worlds,
            Supplier<GlobalDrops> global,
            Function<String, @Nullable ItemStack> items,
            RandomGenerator random) {
        this.mobs = mobs;
        this.worlds = worlds;
        this.global = global;
        this.items = items;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        String id = MobTags.getCustomMobId(event.getEntity()).orElse(null);
        if (id == null) return;
        CustomMob mob = mobs.apply(id);
        DropRules rules = DropRules.of(
                mob == null ? null : mob.mobDrops(),
                worlds.apply(event.getEntity().getWorld()),
                global.get());
        for (Drop drop : rules.roll(random)) {
            ItemStack item = items.apply(drop.itemId());
            if (item == null || drop.amount() <= 0) continue;
            item.setAmount(drop.amount());
            event.getDrops().add(item);
        }
        event.setDroppedExp(rules.experience(event.getDroppedExp()));
    }
}
