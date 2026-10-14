/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime;

import de.eldoria.bloodnight.api.MobContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
     * Starts moving an entity to a location. The move finishes once the target is loaded; on Folia a move into another
     * region always finishes later.
     *
     * @return true, the move has started
     */
    public static boolean teleport(Entity entity, Location location) {
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
