/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.api.event.mob;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A BloodNight mob died. Called after BloodNight added its drops; the drops can still be changed.
 */
public final class CustomMobDeathEvent extends EntityEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String mobId;
    private final @Nullable Entity killer;
    private final List<ItemStack> drops;

    /**
     * @param entity the mob
     * @param mobId  its id
     * @param killer the entity that killed it, if any
     * @param drops  what it drops; changes apply
     */
    public CustomMobDeathEvent(LivingEntity entity, String mobId, @Nullable Entity killer, List<ItemStack> drops) {
        super(entity);
        this.mobId = mobId;
        this.killer = killer;
        this.drops = drops;
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) entity;
    }

    /**
     * @return the mob's id
     */
    public String mobId() {
        return mobId;
    }

    /**
     * @return the entity that killed the mob, if any
     */
    public @Nullable Entity killer() {
        return killer;
    }

    /**
     * @return what the mob drops; changes apply
     */
    public List<ItemStack> drops() {
        return drops;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
