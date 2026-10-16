/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.api.event.BloodNightLootEvent;
import de.eldoria.bloodnight.integration.regions.RegionFlag;
import de.eldoria.bloodnight.integration.regions.RegionRules;
import de.eldoria.bloodnight.items.ItemTexts;
import de.eldoria.bloodnight.loot.Experience;
import de.eldoria.bloodnight.loot.LootLevel;
import de.eldoria.bloodnight.loot.LootRoll;
import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.loot.LootWear;
import de.eldoria.bloodnight.loot.MobLoot;
import de.eldoria.bloodnight.loot.OrdinaryLoot;
import de.eldoria.bloodnight.loot.ServerLoot;
import de.eldoria.bloodnight.loot.WorldLoot;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.VanillaLoot;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.World;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;

/**
 * Changes what mobs a player kills drop; deaths without a player behind them, such as sunlight or lava, keep their
 * vanilla drops.
 * <p>
 * A special mob rolls the loot tables of its own level, its world's and the server's (see {@link LootRoll}), as far as
 * {@code inherit} lets them join, and changes its experience by the most specific level that sets one. Its
 * {@link VanillaLoot} decides whether its vanilla loot stays, is multiplied or is dropped. During a blood night,
 * ordinary enemies drop more vanilla loot and experience and roll their own table lists as their world's
 * {@link OrdinaryLoot} says, unless their kind or spawn reason is excluded or another plugin owns them.
 * <p>
 * The rolled loot passes through {@link BloodNightLootEvent} first. Amounts above an item's stack size are split into
 * full stacks, each as worn as its entry rolls where the entry sets a wear; unknown item ids are left out and unknown
 * table ids are skipped, logged once each.
 * <p>
 * A special mob killed during a blood night also drops the blood night reward where its world says so: a roll of the
 * data pack loot table {@code bloodnight:blood_night/reward}.
 */
public final class DropHandler implements Listener {
    private final Function<String, @Nullable CustomMob> mobs;
    private final Function<World, WorldLoot> worlds;
    private final Supplier<ServerLoot> server;
    private final Function<String, @Nullable LootTable> tables;
    private final Function<String, @Nullable ItemStack> items;
    private final Predicate<World> bloodNight;
    private final ForeignMobs foreign;
    private final RandomGenerator random;
    private final RegionRules regions;
    private final BiFunction<LivingEntity, Player, Collection<ItemStack>> reward;
    private final Consumer<Event> events;
    private final Logger log;
    private final Set<String> missing = ConcurrentHashMap.newKeySet();

    /**
     * @param mobs       the custom mob of an id, if defined
     * @param worlds     the loot settings of a world
     * @param server     the server's loot settings
     * @param tables     the loot table of an id, if defined
     * @param items      a new item of an item id or material key, if known
     * @param bloodNight whether a blood night runs in a world
     * @param foreign    tells mobs of other plugins, whose drops stay as they are
     * @param random     the source of chance
     * @param regions    where regions switch the extra loot of ordinary enemies off
     * @param reward     rolls the blood night reward for a mob and the player who killed it
     * @param events     calls the loot event
     * @param log        where unknown table ids are reported
     */
    public DropHandler(
            Function<String, @Nullable CustomMob> mobs,
            Function<World, WorldLoot> worlds,
            Supplier<ServerLoot> server,
            Function<String, @Nullable LootTable> tables,
            Function<String, @Nullable ItemStack> items,
            Predicate<World> bloodNight,
            ForeignMobs foreign,
            RandomGenerator random,
            RegionRules regions,
            BiFunction<LivingEntity, Player, Collection<ItemStack>> reward,
            Consumer<Event> events,
            Logger log) {
        this.mobs = mobs;
        this.worlds = worlds;
        this.server = server;
        this.tables = tables;
        this.items = items;
        this.bloodNight = bloodNight;
        this.foreign = foreign;
        this.random = random;
        this.regions = regions;
        this.reward = reward;
        this.events = events;
        this.log = log;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;
        WorldLoot world = worlds.apply(entity.getWorld());
        Optional<String> id = MobTags.getCustomMobId(entity);
        if (id.isPresent()) {
            special(event, killer, id.get(), world);
        } else if (isRuledEnemy(entity)) {
            ordinary(event, killer, world);
        }
    }

    private void special(EntityDeathEvent event, Player killer, String id, WorldLoot world) {
        CustomMob mob = mobs.apply(id);
        MobLoot loot = mob == null ? MobLoot.inherited() : mob.loot();
        LivingEntity entity = event.getEntity();
        switch (loot.vanillaLoot()) {
            case KEEP -> {}
            case MULTIPLY ->
                Loot.multiply(entity, event.getDrops(), world.ordinary().lootMultiplier());
            case CLEAR -> Loot.clear(entity, event.getDrops());
        }
        ServerLoot server = this.server.get();
        List<LootLevel> levels = new ArrayList<>();
        levels.add(loot.level());
        if (loot.inherit()) levels.add(world.level());
        if (loot.inherit() && world.inherit()) levels.add(server.level());
        addRolled(event, killer, id, levels);
        if (world.rewardLoot() && bloodNight.test(entity.getWorld())) {
            event.getDrops().addAll(reward.apply(entity, killer));
        }
        Experience experience = firstSet(loot.experience(), world.experience(), server.experience());
        event.setDroppedExp(experience.apply(event.getDroppedExp()));
    }

    private void ordinary(EntityDeathEvent event, Player killer, WorldLoot world) {
        LivingEntity entity = event.getEntity();
        OrdinaryLoot ordinary = world.ordinary();
        if (!ordinary.enabled()
                || ordinary.excludedTypes().contains(entity.getType())
                || ordinary.excludedReasons().contains(entity.getEntitySpawnReason())) return;
        Loot.multiply(entity, event.getDrops(), ordinary.lootMultiplier());
        List<LootLevel> levels = new ArrayList<>();
        levels.add(new LootLevel(ordinary.tables(), ordinary.slots(), false));
        if (world.inherit()) levels.add(server.get().ordinary().level());
        addRolled(event, killer, null, levels);
        event.setDroppedExp(ordinary.experience().apply(event.getDroppedExp()));
    }

    private boolean isRuledEnemy(LivingEntity entity) {
        return entity instanceof Enemy
                && !MobTags.isExtension(entity)
                && foreign.ruled(entity)
                && bloodNight.test(entity.getWorld())
                && regions.allows(entity.getLocation(), RegionFlag.RULES);
    }

    private void addRolled(EntityDeathEvent event, Player killer, @Nullable String id, List<LootLevel> levels) {
        Map<String, List<ItemStack>> loot = new LinkedHashMap<>();
        for (LootRoll.Rolled rolled : LootRoll.roll(levels, this::table, random)) {
            ItemStack item = items.apply(rolled.entry().item());
            if (item == null) continue;
            List<ItemStack> stacks = stacks(item, rolled.amount());
            LootWear wear = rolled.entry().wear();
            if (wear != null) stacks.forEach(stack -> ItemTexts.wear(stack, wear.roll(random)));
            loot.computeIfAbsent(rolled.table(), table -> new ArrayList<>()).addAll(stacks);
        }
        if (loot.isEmpty()) return;
        BloodNightLootEvent lootEvent = new BloodNightLootEvent(event.getEntity(), id, killer, loot);
        events.accept(lootEvent);
        if (lootEvent.isCancelled()) return;
        lootEvent.loot().values().forEach(event.getDrops()::addAll);
    }

    private @Nullable LootTable table(String id) {
        LootTable table = tables.apply(id);
        if (table == null && missing.add(id)) log.warning("Unknown loot table %s is skipped".formatted(id));
        return table;
    }

    /**
     * @param item   the item to drop
     * @param amount how many of it
     * @return full stacks of the item and one stack with the rest
     */
    static List<ItemStack> stacks(ItemStack item, int amount) {
        int size = Math.max(1, item.getMaxStackSize());
        List<ItemStack> stacks = new ArrayList<>();
        for (int left = amount; left > 0; left -= size) {
            ItemStack stack = item.clone();
            stack.setAmount(Math.min(left, size));
            stacks.add(stack);
        }
        return stacks;
    }

    private static Experience firstSet(@Nullable Experience... levels) {
        for (Experience experience : levels) {
            if (experience != null) return experience;
        }
        return Experience.UNCHANGED;
    }
}
