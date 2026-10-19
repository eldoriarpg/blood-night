/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime;

import de.eldoria.bloodnight.api.event.mob.CustomMobMoveEntityEvent;
import de.eldoria.bloodnight.api.internal.MobContext;
import de.eldoria.bloodnight.graph.runtime.world.ProjectileKind;
import de.eldoria.bloodnight.graph.runtime.world.Reach;
import de.eldoria.graph.std.runtime.flow.Delays;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * Helpers generated code calls for nodes that need more than one expression.
 * <p>
 * A behaviour runs on the thread of its mob's region. On Folia other regions belong to other threads, so these helpers
 * only look at places the current thread owns: a query reaching into another region finds nothing, an action there
 * runs on that region's thread a tick later. On Paper every place is owned and everything happens right away.
 */
public final class BukkitRuntime {
    private static final boolean REGIONISED = regionised();
    private static final Set<NamespacedKey> UNFIT_TABLES = ConcurrentHashMap.newKeySet();

    private BukkitRuntime() {}

    /**
     * @param location the centre of an area
     * @param radius   its radius in blocks
     * @return whether the current thread owns the whole area
     */
    public static boolean owns(Location location, double radius) {
        return Bukkit.isOwnedByCurrentRegion(location, (int) Math.ceil(Math.max(radius, 0) / 16));
    }

    /**
     * @return the entities in a box around a location; empty when the box reaches into another region
     */
    public static Collection<Entity> nearbyEntities(Location location, double x, double y, double z) {
        if (!owns(location, Math.max(x, z))) return List.of();
        return location.getWorld().getNearbyEntities(location, x, y, z);
    }

    /**
     * @return the entities in a box around an entity, without it; empty when the box reaches into another region
     */
    public static List<Entity> nearbyEntities(Entity entity, double x, double y, double z) {
        if (!owns(entity.getLocation(), Math.max(x, z))) return List.of();
        return entity.getNearbyEntities(x, y, z);
    }

    /**
     * @return the block at a location, or {@code null} when another region owns it
     */
    public static @Nullable Block blockAt(World world, Location location) {
        Location at = location.clone();
        at.setWorld(world);
        return Bukkit.isOwnedByCurrentRegion(at) ? world.getBlockAt(at) : null;
    }

    /**
     * @return whether the entity sees the other; false when another region owns the other
     */
    public static boolean hasLineOfSight(LivingEntity entity, Entity other) {
        return Bukkit.isOwnedByCurrentRegion(other) && entity.hasLineOfSight(other);
    }

    /**
     * Starts moving an entity to a location, unless a listener of {@link CustomMobMoveEntityEvent} keeps it in place.
     * The move finishes once the target is loaded; on Folia a move into another region always finishes later.
     *
     * @return whether the move has started
     */
    public static boolean teleport(Entity entity, Location location) {
        if (!new CustomMobMoveEntityEvent(entity, CustomMobMoveEntityEvent.Kind.TELEPORT, location).callEvent()) {
            return false;
        }
        entity.teleportAsync(location);
        return true;
    }

    /**
     * Strikes lightning, on the thread owning the location.
     */
    public static void strikeLightning(MobContext context, World world, Location location, boolean effectOnly) {
        context.at(location, () -> {
            if (effectOnly) world.strikeLightningEffect(location);
            else world.strikeLightning(location);
        });
    }

    /**
     * Deals damage of a kind, such as {@code bloodnight:blood_curse}; with a source, it counts as dealt by it.
     */
    public static void damage(LivingEntity target, double amount, @Nullable Entity source, DamageType type) {
        DamageSource.Builder damage = DamageSource.builder(type);
        if (source != null) damage.withCausingEntity(source).withDirectEntity(source);
        target.damage(amount, damage.build());
    }

    /**
     * Rolls a loot table and drops what it gives at a location, on the thread owning it. The mob counts as the looted
     * entity and the player, if any, as its killer, for tables whose conditions ask. Unknown tables drop nothing.
     *
     * @param table  key of the loot table, e.g. {@code bloodnight:blood_night/reward}
     * @param looter the player the loot is for, or {@code null}
     */
    public static void dropLoot(
            MobContext context, World world, Location location, String table, @Nullable Player looter) {
        NamespacedKey key = NamespacedKey.fromString(table);
        LootTable loot = key == null ? null : Bukkit.getLootTable(key);
        if (loot == null) return;
        Location at = location.clone();
        at.setWorld(world);
        LivingEntity mob = context.entity();
        context.at(at, () -> {
            LootContext.Builder builder = new LootContext.Builder(at).lootedEntity(mob);
            if (looter != null) builder.killer(looter);
            for (ItemStack item : rollLoot(loot, builder.build())) world.dropItemNaturally(at, item);
        });
    }

    /**
     * Rolls a loot table, or gives nothing when the table asks for more than the context has, e.g. a table of the type
     * {@code minecraft:block} rolled for a mob; that is logged once per table. The plugin's tables are of the type
     * {@code minecraft:entity}, which needs the looted entity and nothing else.
     */
    public static Collection<ItemStack> rollLoot(LootTable table, LootContext context) {
        try {
            return table.populateLoot(ThreadLocalRandom.current(), context);
        } catch (IllegalArgumentException e) {
            if (UNFIT_TABLES.add(table.getKey())) {
                Bukkit.getLogger()
                        .warning("Loot table %s does not fit a mob's loot: %s"
                                .formatted(table.getKey(), e.getMessage()));
            }
            return List.of();
        }
    }

    /**
     * Marks TNT whose explosion breaks no blocks; the plugin clears the block list of explosions of entities with it.
     */
    public static final NamespacedKey HARMLESS = new NamespacedKey("bloodnight", "harmless_explosion");

    /**
     * Spawns lit TNT, on the thread owning the location. Its explosion hurts but breaks no blocks, and counts as the
     * mob's.
     *
     * @param seconds how long the fuse burns
     * @param power   how strong the explosion is, 4 for normal TNT
     */
    public static void spawnTnt(MobContext context, Location location, double seconds, double power) {
        LivingEntity mob = context.entity();
        context.at(location, () -> location.getWorld().spawn(location, TNTPrimed.class, tnt -> {
            tnt.setFuseTicks((int) Math.round(Math.max(seconds, 0) * 20));
            tnt.setYield((float) Math.max(power, 0));
            tnt.setSource(mob);
            tnt.getPersistentDataContainer().set(HARMLESS, PersistentDataType.BOOLEAN, true);
        }));
    }

    /**
     * Spawns a vanilla entity, when the current thread owns the location.
     *
     * @return the entity, or {@code null} when another region owns the location
     */
    public static @Nullable Entity spawnEntity(Location location, EntityType type) {
        if (!Bukkit.isOwnedByCurrentRegion(location)) return null;
        return location.getWorld().spawnEntity(location, type, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    /**
     * Breaks the blocks within a radius around a location, as a wither does: blocks tagged
     * {@code minecraft:wither_immune}, such as bedrock, stay, nothing breaks while the world's {@code mob_griefing}
     * rule is off, and every block is offered to protection plugins first as an {@link EntityChangeBlockEvent} of the
     * mob. Blocks other regions own stay.
     *
     * @param drops whether the blocks drop as items
     * @return how many blocks broke
     */
    public static int breakBlocks(MobContext context, Location center, double radius, boolean drops) {
        World world = center.getWorld();
        if (Boolean.FALSE.equals(world.getGameRuleValue(GameRules.MOB_GRIEFING))) return 0;
        LivingEntity mob = context.entity();
        BlockData air = Material.AIR.createBlockData();
        int reach = (int) Math.ceil(Math.max(radius, 0));
        int broken = 0;
        for (int x = -reach; x <= reach; x++) {
            for (int y = -reach; y <= reach; y++) {
                for (int z = -reach; z <= reach; z++) {
                    if (x * x + y * y + z * z > radius * radius) continue;
                    Block block = center.clone().add(x, y, z).getBlock();
                    if (!Bukkit.isOwnedByCurrentRegion(block.getLocation())) continue;
                    if (block.isEmpty() || Tag.WITHER_IMMUNE.isTagged(block.getType())) continue;
                    if (!new EntityChangeBlockEvent(mob, block, air).callEvent()) continue;
                    if (drops) block.breakNaturally();
                    else block.setType(Material.AIR);
                    broken++;
                }
            }
        }
        return broken;
    }

    /**
     * Runs a command as the console, on the global region. {@code {player}} is the player's name, {@code {world}},
     * {@code {x}}, {@code {y}} and {@code {z}} where the mob stands.
     *
     * @param player the player the command is about, or {@code null}
     */
    public static void runCommand(MobContext context, String command, @Nullable Player player) {
        String filled = fill(context, command, player);
        context.global(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), filled));
    }

    /**
     * Runs a command as a player, on the thread owning the player, with the same placeholders as
     * {@link #runCommand}.
     */
    public static void playerCommand(MobContext context, Player player, String command) {
        String filled = fill(context, command, player);
        context.at(player.getLocation(), () -> player.performCommand(filled));
    }

    /**
     * Sends a MiniMessage text to the players in the mob's world or on the whole server.
     */
    public static void broadcast(MobContext context, String text, Reach reach) {
        Component message = MiniMessage.miniMessage().deserialize(text);
        World world = context.entity().getWorld();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (reach == Reach.SERVER || player.getWorld().equals(world)) player.sendMessage(message);
        }
    }

    private static String fill(MobContext context, String command, @Nullable Player player) {
        Location location = context.entity().getLocation();
        String filled = command.replace("{world}", location.getWorld().getName())
                .replace("{x}", Integer.toString(location.getBlockX()))
                .replace("{y}", Integer.toString(location.getBlockY()))
                .replace("{z}", Integer.toString(location.getBlockZ()));
        return player == null ? filled : filled.replace("{player}", player.getName());
    }

    /**
     * Spawns particles, on the thread owning the location.
     *
     * @param data what the particle needs, or {@code null} for particles without data
     */
    public static void particles(
            MobContext context,
            World world,
            Particle particle,
            Location location,
            int count,
            double offsetX,
            double offsetY,
            double offsetZ,
            @Nullable Object data) {
        context.at(location, () -> {
            if (data == null) world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ);
            else world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, data);
        });
    }

    /**
     * Spawns particles, flying off at a speed whose meaning depends on the particle.
     */
    public static void particles(
            MobContext context,
            World world,
            Particle particle,
            Location location,
            int count,
            double offsetX,
            double offsetY,
            double offsetZ,
            double speed,
            @Nullable Object data) {
        context.at(
                location, () -> world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, data));
    }

    /**
     * Plays a particle effect of the library and, if it has something that travels, runs a chain once that reaches
     * the target. The chain is held back by the behaviour's delays, so it runs on the mob's thread.
     *
     * @param delays  the behaviour's delays
     * @param arrived what runs once the effect arrives, or {@code null}
     */
    public static void effect(
            MobContext context,
            Delays delays,
            String id,
            Object source,
            @Nullable Object target,
            @Nullable Player viewer,
            @Nullable Runnable arrived) {
        long ticks = context.effect(id, source, target, viewer);
        if (ticks >= 0 && arrived != null) delays.after(ticks * 50, arrived);
    }

    /**
     * Reads something about a whole world, which only a server without regions can tell.
     *
     * @param property what is read, for the failure
     * @param read     the read
     * @return what was read
     * @throws IllegalStateException on Folia, where no thread owns a whole world
     */
    public static <T> T wholeWorld(String property, Supplier<T> read) {
        if (REGIONISED) {
            throw new IllegalStateException(
                    "The world's %s are not available on Folia, where every region runs on its own thread"
                            .formatted(property));
        }
        return read.get();
    }

    private static boolean regionised() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Launches a projectile from a shooter straight at a target.
     *
     * @param shooter the launching entity
     * @param target  what to aim at
     * @param kind    the projectile
     * @param speed   blocks per tick
     * @return the projectile, or {@code null} when another region owns the target and nothing was launched
     */
    public static @Nullable Projectile launchAt(
            LivingEntity shooter, Entity target, ProjectileKind kind, double speed) {
        if (!Bukkit.isOwnedByCurrentRegion(target)) return null;
        Vector direction = target.getLocation()
                .toVector()
                .subtract(shooter.getEyeLocation().toVector());
        if (direction.lengthSquared() == 0) direction = shooter.getLocation().getDirection();
        return shooter.launchProjectile(kind.type(), direction.normalize().multiply(speed));
    }

    /**
     * Spawns a lingering cloud applying a potion to everything inside, on the thread owning the location.
     *
     * @param context       the behaviour's mob
     * @param location      centre of the cloud
     * @param potion        the potion applied
     * @param seconds       how long the cloud stays
     * @param radius        starting radius in blocks
     * @param radiusPerTick growth per tick; negative values shrink the cloud
     * @param color         particle colour, or {@code null} for the potion's
     */
    public static void potionCloud(
            MobContext context,
            Location location,
            PotionType potion,
            int seconds,
            double radius,
            double radiusPerTick,
            @Nullable Color color) {
        context.at(location, () -> location.getWorld().spawn(location, AreaEffectCloud.class, cloud -> {
            cloud.setBasePotionType(potion);
            cloud.setDuration(seconds * 20);
            cloud.setRadius((float) radius);
            cloud.setRadiusPerTick((float) radiusPerTick);
            if (color != null) cloud.setColor(color);
        }));
    }

    /**
     * @param location     where to search from
     * @param radius       search radius in blocks
     * @param survivalOnly whether players in other game modes are ignored
     * @return the closest player, or {@code null} when none is in range or the range reaches into another region
     */
    public static @Nullable Player nearestPlayer(Location location, double radius, boolean survivalOnly) {
        if (!owns(location, radius)) return null;
        return location.getWorld().getNearbyPlayers(location, radius).stream()
                .filter(player -> !survivalOnly || player.getGameMode() == GameMode.SURVIVAL)
                .min(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(location)))
                .orElse(null);
    }

    /**
     * The entity behind damage: what caused it, such as the player who shot an arrow, the igniter of TNT or the
     * owner of a potion cloud; otherwise the damaging entity itself.
     *
     * @param event a damage event
     * @return the attacker, or {@code null} when no entity is involved
     */
    public static @Nullable Entity attacker(EntityDamageEvent event) {
        Entity causing = event.getDamageSource().getCausingEntity();
        if (causing != null) return causing;
        return event instanceof EntityDamageByEntityEvent byEntity ? byEntity.getDamager() : null;
    }

    /**
     * @param event a death event
     * @return the entity that caused the death, or {@code null} when none did
     */
    public static @Nullable Entity attacker(EntityDeathEvent event) {
        return event.getDamageSource().getCausingEntity();
    }

    /**
     * @param event a death event
     * @return the entity that caused the death unless it is the victim itself, or {@code null}
     */
    public static @Nullable Entity killer(EntityDeathEvent event) {
        Entity killer = attacker(event);
        return killer == event.getEntity() ? null : killer;
    }
}
