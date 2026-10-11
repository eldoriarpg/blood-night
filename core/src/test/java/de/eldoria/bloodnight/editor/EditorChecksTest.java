/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.elements.GlobalDrops;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.graph.model.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditorChecksTest {
    private static CustomMob armed(String id, String item) {
        CustomMob mob = Mobs.zombie(id, Graph.builder().build());
        return new CustomMob(
                mob.id(),
                new Equipment(null, null, null, null, item, null),
                mob.attributes(),
                mob.behaviour(),
                mob.mobDrops(),
                mob.extension(),
                mob.spawn());
    }

    @Test
    void mobsNeedTheirIdAndKnownItems() {
        EditorResult result = new EditorResult(
                Map.of("a", armed("a", "sword"), "b", armed("other", "axe")), List.of(), null, Map.of());
        assertEquals(
                List.of("Mob other is stored as b", "Mob other uses unknown item axe"),
                EditorChecks.problems(result, Set.of("sword")).stream().sorted().toList());
    }

    @Test
    void unknownWorldsAreFound() {
        var world = new SettingsPayload.WorldPayload("w", new WorldSettings());
        EditorResult result = new EditorResult(
                Map.of(),
                List.of(),
                new SettingsPayload(
                        new ConfigFile(), GlobalDrops.defaultDrops(), Map.of("known", world, "gone", world)),
                Map.of());
        assertEquals(List.of("gone"), EditorChecks.unknownWorlds(result, Set.of("known")));
    }

    @Test
    void filesChangedSinceTheSessionOpenedAreFound() {
        Map<String, String> opened = Map.of("mobs.yml", "a", "config.yml", "b");
        EditorResult result = new EditorResult(Map.of(), List.of(), null, opened);
        assertEquals(List.of(), EditorChecks.changedFiles(result, opened));
        assertEquals(
                List.of("mobs.yml"), EditorChecks.changedFiles(result, Map.of("mobs.yml", "c", "config.yml", "b")));
        EditorResult lost = new EditorResult(Map.of(), List.of(), null, Map.of());
        assertEquals(List.of("config.yml", "mobs.yml"), EditorChecks.changedFiles(lost, opened));
    }
}
