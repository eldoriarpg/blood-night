/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Drop;
import de.eldoria.bloodnight.mob.meta.Equipment;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Checks a result from the editor against the server before it is applied: each mob is stored under its own id, and
 * mobs only use registered items. Worlds the server does not know are reported separately, they are skipped.
 */
public final class EditorChecks {
    private EditorChecks() {}

    /**
     * @param result the result
     * @param items  the ids of the registered items
     * @return why the result cannot be applied; empty when it can
     */
    public static List<String> problems(EditorResult result, Set<String> items) {
        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, CustomMob> entry : result.mobs().entrySet()) {
            CustomMob mob = entry.getValue();
            if (!entry.getKey().equals(mob.id())) {
                problems.add("Mob %s is stored as %s".formatted(mob.id(), entry.getKey()));
            }
            for (String item : usedItems(mob)) {
                if (!items.contains(item)) problems.add("Mob %s uses unknown item %s".formatted(mob.id(), item));
            }
        }
        return problems;
    }

    /**
     * @param result the result
     * @param worlds the UIDs of the server's worlds
     * @return the UIDs of worlds in the result the server does not know
     */
    public static List<String> unknownWorlds(EditorResult result, Set<String> worlds) {
        SettingsPayload settings = result.settings();
        if (settings == null) return List.of();
        return settings.worlds().keySet().stream()
                .filter(world -> !worlds.contains(world))
                .sorted()
                .toList();
    }

    private static List<String> usedItems(CustomMob mob) {
        List<String> items = new ArrayList<>(slots(mob.equipment()));
        items.addAll(slots(mob.extension().equipment()));
        if (mob.mobDrops() != null)
            mob.mobDrops().dropList().stream().map(Drop::itemId).forEach(items::add);
        return items.stream().filter(Objects::nonNull).distinct().toList();
    }

    private static List<String> slots(@Nullable Equipment equipment) {
        if (equipment == null) return List.of();
        return Arrays.asList(
                equipment.head(),
                equipment.chest(),
                equipment.legs(),
                equipment.feet(),
                equipment.mainHand(),
                equipment.offHand());
    }
}
