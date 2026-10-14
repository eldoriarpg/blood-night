/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.configuration.elements.GlobalDrops;
import de.eldoria.bloodnight.configuration.elements.world.VanillaDrops;
import de.eldoria.bloodnight.configuration.elements.world.WorldDrops;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.VanillaLoot;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.World;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

/**
 * Changes what mobs a player kills drop; deaths without a player behind them, such as sunlight or lava, keep their
 * vanilla drops.
 * <p>
 * A special mob adds its configured drops to its vanilla loot and changes its experience (see {@link DropRules}); its
 * {@link VanillaLoot} decides whether that loot stays, is multiplied or is dropped. During a blood night, ordinary
 * enemies drop more loot and experience as their world's {@link VanillaDrops} say, unless their kind or spawn reason
 * is excluded or another plugin owns them. Unknown item ids are left out.
 */
public final class DropHandler implements Listener {
    private final Function<String, @Nullable CustomMob> mobs;
    private final Function<World, WorldDrops> worlds;
    private final Supplier<GlobalDrops> global;
    private final Function<String, @Nullable ItemStack> items;
    private final Predicate<World> bloodNight;
    private final ForeignMobs foreign;
    private final RandomGenerator random;

    /**
     * @param mobs       the custom mob of an id, if defined
     * @param worlds     the drop settings of a world
     * @param global     the global drop settings
     * @param items      a new item of an item id or material key, if known
     * @param bloodNight whether a blood night runs in a world
     * @param foreign    tells mobs of other plugins, whose drops stay as they are
     * @param random     the source of chance
     */
    public DropHandler(
            Function<String, @Nullable CustomMob> mobs,
            Function<World, WorldDrops> worlds,
            Supplier<GlobalDrops> global,
            Function<String, @Nullable ItemStack> items,
            Predicate<World> bloodNight,
            ForeignMobs foreign,
            RandomGenerator random) {
        this.mobs = mobs;
        this.worlds = worlds;
        this.global = global;
        this.items = items;
        this.bloodNight = bloodNight;
        this.foreign = foreign;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getKiller() == null) return;
        WorldDrops world = worlds.apply(entity.getWorld());
        Optional<String> id = MobTags.getCustomMobId(entity);
        if (id.isPresent()) {
            special(event, mobs.apply(id.get()), world);
        } else if (isRuledEnemy(entity)) {
            ordinary(event, world);
        }
    }

    private void special(EntityDeathEvent event, @Nullable CustomMob mob, WorldDrops world) {
        MobDrops drops = mob == null ? null : mob.mobDrops();
        switch (drops == null ? VanillaLoot.KEEP : drops.vanillaLoot()) {
            case KEEP -> {}
            case MULTIPLY ->
                Loot.multiply(
                        event.getEntity(), event.getDrops(), world.vanilla().lootMultiplier());
            case CLEAR -> Loot.clear(event.getEntity(), event.getDrops());
        }
        DropRules rules = DropRules.of(drops, world, global.get());
        addRolled(event, rules);
        event.setDroppedExp(rules.experience(event.getDroppedExp()));
    }

    private void ordinary(EntityDeathEvent event, WorldDrops world) {
        LivingEntity entity = event.getEntity();
        VanillaDrops vanilla = world.vanilla();
        if (!vanilla.enabled()
                || vanilla.excludedTypes().contains(entity.getType())
                || vanilla.excludedReasons().contains(entity.getEntitySpawnReason())) return;
        Loot.multiply(entity, event.getDrops(), vanilla.lootMultiplier());
        if (vanilla.extraDrops()) addRolled(event, DropRules.of(null, world, global.get()));
        event.setDroppedExp((int) Math.round(vanilla.xpModifier().apply(event.getDroppedExp(), vanilla.xpValue())));
    }

    private boolean isRuledEnemy(LivingEntity entity) {
        return entity instanceof Enemy
                && !MobTags.isExtension(entity)
                && foreign.ruled(entity)
                && bloodNight.test(entity.getWorld());
    }

    private void addRolled(EntityDeathEvent event, DropRules rules) {
        for (Drop drop : rules.roll(random)) {
            ItemStack item = items.apply(drop.itemId());
            if (item == null || drop.amount() <= 0) continue;
            item.setAmount(drop.amount());
            event.getDrops().add(item);
        }
    }
}
