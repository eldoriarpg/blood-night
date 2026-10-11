/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.mob.meta.Despawn;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.Entity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.OptionalLong;
import java.util.Set;

/**
 * Removes custom mobs that despawn at night end once the blood night they spawned in is over, together with their
 * extension.
 * <p>
 * {@link #sweep()} runs every second after the night check, so a mob's night end entry has run before it goes and a
 * blood night picked up again after a restart keeps its mobs. Mobs loaded later, e.g. with their chunk, are found by
 * the next sweep. {@link #tick()} removes at most {@value #PER_TICK} mobs per tick.
 */
public final class NightDespawner {
    static final int PER_TICK = 10;
    private final MobCoordinator coordinator;
    private final BloodNights nights;
    private final Deque<ActiveMob> queue = new ArrayDeque<>();
    private final Set<Integer> queued = new HashSet<>();

    /**
     * @param coordinator the living custom mobs
     * @param nights      the running blood nights
     */
    public NightDespawner(MobCoordinator coordinator, BloodNights nights) {
        this.coordinator = coordinator;
        this.nights = nights;
    }

    /**
     * Queues every mob whose blood night is over.
     */
    public void sweep() {
        for (ActiveMob mob : coordinator.active()) {
            if (isOver(mob) && queued.add(mob.entity().getEntityId())) queue.add(mob);
        }
    }

    /**
     * Removes queued mobs.
     */
    public void tick() {
        for (int i = 0; i < PER_TICK && !queue.isEmpty(); i++) {
            ActiveMob mob = queue.poll();
            queued.remove(mob.entity().getEntityId());
            remove(mob);
        }
    }

    private boolean isOver(ActiveMob mob) {
        if (mob.mob().spawn().despawn() != Despawn.NIGHT_END || !mob.entity().isValid()) return false;
        OptionalLong night = MobTags.getNight(mob.entity());
        if (night.isEmpty()) return false;
        OptionalLong running = nights.running(mob.entity().getWorld());
        return running.isEmpty() || running.getAsLong() != night.getAsLong();
    }

    private void remove(ActiveMob mob) {
        Entity extension = mob.extension();
        if (extension != null) extension.remove();
        mob.entity().remove();
        coordinator.remove(mob.entity());
    }
}
