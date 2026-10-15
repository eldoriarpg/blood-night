/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.loot.LootAmount;
import de.eldoria.bloodnight.loot.LootEntry;
import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.Macro;
import de.eldoria.graph.model.NodeInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EditorResultTest {
    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void whatTheEditorGetsComesBack() {
        CustomMob mob = Mobs.zombie(
                "test",
                Graph.builder().node("tick", NodeInstance.of("mob.tick")).build());
        LootTable gems = new LootTable(
                "Gems",
                List.of(new LootEntry(
                        "minecraft:diamond",
                        2,
                        new LootAmount(LootAmount.AmountMode.CHOICES, 1, 1, Map.of(2, 1, 5, 3)),
                        true)));
        WorldSettings world = EditorPayload.MAPPER.readValue(
                "{\"enabled\":false,\"nightSettings\":{\"begin\":12000}}", WorldSettings.class);
        EditorPayload payload = new EditorPayload(
                "abc",
                Map.of("test", mob),
                List.of(Macro.of("burst", Graph.builder().build())),
                Map.of(),
                Map.of("gems", gems),
                Behaviours.empty().problems(),
                Map.of(),
                Map.of(),
                new SettingsPayload(
                        new ConfigFile(),
                        Map.of("uid", new SettingsPayload.WorldPayload("world", "NORMAL", true, world))),
                new EditorChoices(
                        "26.3",
                        List.of("minecraft:stone"),
                        Map.of("minecraft:diamond_sword", 1),
                        List.of("minecraft:sharpness"),
                        Map.of("CREEPER", "org.bukkit.entity.Creeper"),
                        List.of("CREEPER"),
                        List.of("SkeletalKnight"),
                        List.of("minecraft:max_health"),
                        List.of("minecraft:desert"),
                        List.of("minecraft:darkness"),
                        Map.of(
                                "minecraft:spawn_phantoms",
                                new EditorChoices.GameRuleChoice("boolean", "gamerule.doInsomnia")),
                        List.of("home", "spawn")),
                Map.of("mobs.yml", "abc"),
                EditorStore.empty(),
                false,
                null);

        EditorResult result = EditorResult.of(EditorPayload.MAPPER.readTree(payload.asJson()));

        assertEquals(mob, result.mobs().get("test"));
        assertEquals(Map.of("mobs.yml", "abc"), result.hashes());
        assertEquals("burst", result.macros().getFirst().id());
        assertEquals(Map.of("gems", gems), result.lootTables());
        WorldSettings back = result.settings().worlds().get("uid").settings();
        assertEquals(false, back.enabled());
        assertEquals(12000, back.nightSettings().begin());
        assertEquals(
                "<dark_red>[<red>BN</red>]</dark_red> ",
                result.settings().main().announcements().prefix());
    }
}
