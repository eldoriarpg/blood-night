/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event.mob;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A player killed a BloodNight mob, or an ordinary enemy during a blood night, and BloodNight rolled its loot tables
 * and the money the kill pays through Vault. Called before the rolled loot joins the drops and before the money is
 * paid: the stacks can be changed, added or removed and the money changed, and cancelling it drops none of the stacks
 * and pays nothing. The money is split among the players who damaged the mob, by the damage each dealt. Vanilla loot,
 * the blood night reward and experience are not part of it.
 * <p>
 * Called on the dying entity's thread, during its death event.
 */
public final class BloodNightLootEvent extends EntityEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final @Nullable String mobId;
    private final Player killer;
    private final Map<String, List<ItemStack>> loot;
    private double money;
    private boolean cancelled;

    /**
     * @param entity the mob that died
     * @param mobId  its BloodNight mob id, or null for an ordinary enemy
     * @param killer the player who killed it
     * @param loot   the rolled stacks by the id of the table they came from; changes apply
     * @param money  the money the kill pays, 0 for none
     */
    public BloodNightLootEvent(
            LivingEntity entity,
            @Nullable String mobId,
            Player killer,
            Map<String, List<ItemStack>> loot,
            double money) {
        super(entity);
        this.mobId = mobId;
        this.killer = killer;
        this.loot = loot;
        this.money = money;
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    /**
     * @return the BloodNight mob id, or null for an ordinary enemy
     */
    public @Nullable String mobId() {
        return mobId;
    }

    /**
     * @return the player who killed the mob
     */
    public Player killer() {
        return killer;
    }

    /**
     * @return the rolled stacks by the id of the table they came from; the map and its lists can be changed
     */
    public Map<String, List<ItemStack>> loot() {
        return loot;
    }

    /**
     * @return the money the kill pays, 0 for none
     */
    public double money() {
        return money;
    }

    /**
     * @param money the money the kill pays instead; 0 or less pays nothing
     */
    public void setMoney(double money) {
        this.money = Math.max(0, money);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
