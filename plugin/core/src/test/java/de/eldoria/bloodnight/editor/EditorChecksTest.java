/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.loot.LootEntry;
import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.loot.MobLoot;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.MobTypes;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.Extension;
import de.eldoria.bloodnight.mob.meta.ExtensionType;
import de.eldoria.graph.model.Graph;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorChecksTest {
    private static CustomMob armed(String id, String item) {
        CustomMob mob = Mobs.zombie(id, Graph.builder().build());
        return new CustomMob(
                mob.id(),
                new Equipment(null, null, null, null, item, null),
                mob.attributes(),
                mob.behaviour(),
                mob.loot(),
                mob.extension(),
                mob.spawn());
    }

    @Test
    void mobsNeedTheirIdAndKnownItems() {
        EditorResult result = new EditorResult(
                Map.of("a", armed("a", "sword"), "b", armed("other", "axe")),
                List.of(),
                Map.of(),
                Map.of(),
                null,
                Map.of());
        assertEquals(
                List.of("Mob other is stored as b", "Mob other uses unknown item axe"),
                EditorChecks.problems(result, Set.of("sword")::contains).stream()
                        .sorted()
                        .toList());
    }

    @Test
    void mobsAndTheirExtensionsAreMobs() {
        CustomMob zombie = Mobs.zombie("spit", Graph.builder().build());
        Attributes attributes = zombie.attributes();
        CustomMob spit = new CustomMob(
                zombie.id(),
                zombie.equipment(),
                new Attributes(
                        attributes.name(),
                        EntityType.LLAMA_SPIT,
                        attributes.exactType(),
                        attributes.healthModifier(),
                        attributes.health(),
                        attributes.damageModifier(),
                        attributes.damage(),
                        attributes.changes()),
                zombie.behaviour(),
                zombie.loot(),
                new Extension(ExtensionType.CARRIER, EntityType.OAK_BOAT, null),
                zombie.spawn());
        EditorResult result = new EditorResult(Map.of("spit", spit), List.of(), Map.of(), Map.of(), null, Map.of());

        assertEquals(
                List.of(
                        "Mob spit cannot be a LLAMA_SPIT, which is no mob",
                        "The extension of mob spit cannot be a OAK_BOAT, which is no mob"),
                EditorChecks.problems(result, item -> true));
        assertTrue(MobTypes.isMob(EntityType.ZOMBIE));
        assertFalse(MobTypes.isMob(EntityType.ARMOR_STAND));
    }

    @Test
    void lootTablesNeedKnownItemsAndAssignmentsKnownTables() {
        CustomMob mob = Mobs.zombie("a", Graph.builder().build())
                .withLoot(new MobLoot(List.of("gems", "gone"), null, null, null, null, null));
        LootTable gems = new LootTable("Gems", List.of(new LootEntry("ruby", 1, null, false)));
        EditorResult result =
                new EditorResult(Map.of("a", mob), List.of(), Map.of(), Map.of("gems", gems), null, Map.of());

        assertEquals(
                List.of("Loot table gems uses unknown item ruby", "Mob a uses unknown loot table gone"),
                EditorChecks.problems(result, Set.of()::contains).stream()
                        .sorted()
                        .toList());
    }

    @Test
    void unknownWorldsAreFound() {
        var world = new SettingsPayload.WorldPayload("w", "NORMAL", true, new WorldSettings());
        EditorResult result = new EditorResult(
                Map.of(),
                List.of(),
                Map.of(),
                Map.of(),
                new SettingsPayload(new ConfigFile(), Map.of("known", world, "gone", world)),
                Map.of());
        assertEquals(List.of("gone"), EditorChecks.unknownWorlds(result, Set.of("known")));
    }

    @Test
    void filesChangedSinceTheSessionOpenedAreFound() {
        Map<String, String> opened = Map.of("mobs.yml", "a", "config.yml", "b");
        EditorResult result = new EditorResult(Map.of(), List.of(), Map.of(), Map.of(), null, opened);
        assertEquals(List.of(), EditorChecks.changedFiles(result, opened));
        assertEquals(
                List.of("mobs.yml"), EditorChecks.changedFiles(result, Map.of("mobs.yml", "c", "config.yml", "b")));
        EditorResult lost = new EditorResult(Map.of(), List.of(), Map.of(), Map.of(), null, Map.of());
        assertEquals(List.of("config.yml", "mobs.yml"), EditorChecks.changedFiles(lost, opened));
    }

    @Test
    void hashesAreReadFromThePayload() {
        var payload = EditorPayload.MAPPER.readTree("{\"hashes\":{\"mobs.yml\":\"a\",\"broken\":1}}");
        assertEquals(Map.of("mobs.yml", "a"), EditorChecks.hashes(payload));
        assertEquals(Map.of(), EditorChecks.hashes(EditorPayload.MAPPER.readTree("{}")));
    }
}
