/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.loot.assign;

import de.eldoria.bloodnight.mob.attributes.ValueModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * What ordinary enemies a player kills during a blood night drop in a world. Equipment they wear or hold is never
 * multiplied, only their loot.
 *
 * @param enabled         whether their drops change at all
 * @param lootMultiplier  how much of their vanilla loot falls, e.g. 2 for twice the amount
 * @param tables          the ids of the loot tables they roll on top; the server's ordinary tables join them
 * @param slots           how many entries drop, as weights by count; null takes the server's
 * @param xpModifier      how their experience changes
 * @param xpValue         the number it changes by
 * @param excludedTypes   kinds of mobs whose drops never change
 * @param excludedReasons mobs spawned for these reasons keep their drops, e.g. from spawners or through portals
 */
public record OrdinaryLoot(
        Boolean enabled,
        Double lootMultiplier,
        List<String> tables,
        @Nullable Map<Integer, Integer> slots,
        ValueModifier xpModifier,
        Double xpValue,
        Set<EntityType> excludedTypes,
        Set<SpawnReason> excludedReasons) {
    private static final Set<SpawnReason> EXCLUDED_REASONS = Collections.unmodifiableSet(
            EnumSet.of(SpawnReason.SPAWNER, SpawnReason.TRIAL_SPAWNER, SpawnReason.NETHER_PORTAL));

    public OrdinaryLoot {
        enabled = Objects.requireNonNullElse(enabled, true);
        lootMultiplier = Objects.requireNonNullElse(lootMultiplier, 2.0);
        tables = List.copyOf(Objects.requireNonNullElse(tables, List.of()));
        slots = slots == null ? null : Map.copyOf(slots);
        xpModifier = Objects.requireNonNullElse(xpModifier, ValueModifier.MULTIPLY);
        xpValue = Objects.requireNonNullElse(xpValue, 2.0);
        excludedTypes = ordered(excludedTypes, EntityType.class);
        excludedReasons = excludedReasons == null ? EXCLUDED_REASONS : ordered(excludedReasons, SpawnReason.class);
    }

    /**
     * @return the settings of a fresh world
     */
    public static OrdinaryLoot defaults() {
        return new OrdinaryLoot(true, 2.0, List.of(), null, ValueModifier.MULTIPLY, 2.0, Set.of(), EXCLUDED_REASONS);
    }

    /**
     * @return how their experience changes
     */
    public Experience experience() {
        return new Experience(xpModifier, xpValue);
    }

    private static <E extends Enum<E>> Set<E> ordered(@Nullable Set<E> values, Class<E> type) {
        EnumSet<E> copy = EnumSet.noneOf(type);
        if (values != null) copy.addAll(values);
        return Collections.unmodifiableSet(copy);
    }
}
