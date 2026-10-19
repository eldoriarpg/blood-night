/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime.world;

import de.eldoria.bloodnight.api.internal.MobContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Mobs carrying entities, as generated code calls it: picking one up, lifting it and dropping it.
 * <p>
 * A carried entity is marked {@link #HELD} with its carrier's id; the plugin keeps it from getting off on its own until
 * it is dropped, its carrier dies, or it struggles free: a player who presses sneak five times breaks loose. Each
 * press is counted in {@link #STRUGGLES}.
 */
public final class Carrying {
    /**
     * Marks a carried entity with the id of its carrier.
     */
    public static final NamespacedKey HELD = new NamespacedKey("bloodnight", "carried_by");

    /**
     * How often a carried player has pressed sneak to break loose.
     */
    public static final NamespacedKey STRUGGLES = new NamespacedKey("bloodnight", "struggles");

    private static final double HEADROOM = 3;

    private Carrying() {}

    /**
     * Puts an entity on the carrier, taking it off whatever it rode before.
     *
     * @return whether the carrier now carries it; false for the carrier itself and entities of another region
     */
    public static boolean carry(Entity carrier, Entity passenger) {
        if (carrier.equals(passenger) || !Bukkit.isOwnedByCurrentRegion(passenger)) return false;
        Entity vehicle = passenger.getVehicle();
        if (vehicle != null && !vehicle.equals(carrier) && !vehicle.removePassenger(passenger)) return false;
        if (!carrier.getPassengers().contains(passenger) && !carrier.addPassenger(passenger)) return false;
        passenger
                .getPersistentDataContainer()
                .set(HELD, PersistentDataType.STRING, carrier.getUniqueId().toString());
        passenger.getPersistentDataContainer().remove(STRUGGLES);
        return true;
    }

    /**
     * Lets go of everything the carrier carries.
     */
    public static void drop(Entity carrier) {
        for (Entity passenger : List.copyOf(carrier.getPassengers())) {
            unmark(passenger);
            carrier.removePassenger(passenger);
        }
    }

    /**
     * Lets a carried entity get off, e.g. once it struggled free.
     */
    public static void release(Entity passenger) {
        unmark(passenger);
        Entity vehicle = passenger.getVehicle();
        if (vehicle != null) vehicle.removePassenger(passenger);
    }

    /**
     * Removes the marks of being carried.
     */
    public static void unmark(Entity passenger) {
        passenger.getPersistentDataContainer().remove(HELD);
        passenger.getPersistentDataContainer().remove(STRUGGLES);
    }

    /**
     * @return whether the entity is held by the vehicle it is getting off
     */
    public static boolean holds(Entity vehicle, Entity passenger) {
        String carrier = passenger.getPersistentDataContainer().get(HELD, PersistentDataType.STRING);
        return carrier != null && carrier.equals(vehicle.getUniqueId().toString());
    }

    /**
     * Moves an entity straight up with everything it carries, a little each tick, until it rose the height or a block
     * is in the way. A mob does not steer while it rises, so its own flight cannot pull it back.
     *
     * @param height  how far up, in blocks
     * @param seconds how long the rise takes
     */
    public static void lift(MobContext context, Entity target, double height, double seconds) {
        int ticks = (int) Math.max(1, Math.round(seconds * 20));
        double step = Math.max(height, 0) / ticks;
        boolean aware = !(target instanceof Mob mob) || mob.isAware();
        if (target instanceof Mob mob) mob.setAware(false);
        rise(context, target, step, ticks, aware);
    }

    private static void rise(MobContext context, Entity target, double step, int remaining, boolean aware) {
        context.later(
                target,
                1,
                () -> {
                    Location next = target.getLocation().add(0, step, 0);
                    boolean blocked = next.clone()
                            .add(0, target.getHeight() + HEADROOM, 0)
                            .getBlock()
                            .isSolid();
                    if (remaining <= 0 || blocked) {
                        if (target instanceof Mob mob) mob.setAware(aware);
                        return;
                    }
                    target.teleportAsync(next, TeleportCause.PLUGIN);
                    rise(context, target, step, remaining - 1, aware);
                },
                () -> {});
    }
}
