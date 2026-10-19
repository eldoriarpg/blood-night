/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.editor.payload.EditorPayload;
import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.spawn.SpawnRules;
import de.eldoria.bloodnight.store.files.MobPackage;
import de.eldoria.bloodnight.store.files.StoreFiles;
import de.eldoria.bloodnight.store.files.StorePack;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import tools.jackson.databind.node.DoubleNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobStoreTest {
    private static final Graph OLD = Graph.builder().build();
    private static final Graph NEW =
            Graph.builder().node("tick", NodeInstance.of("mob.tick")).build();
    private static final Map<String, LootTable> TABLES = Map.of(
            "runner_loot", new LootTable("Runner", List.of()),
            "runner_bones", new LootTable("Bones", List.of()));
    private static final Graph EDITED =
            Graph.builder().node("spawn", NodeInstance.of("mob.spawn")).build();

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static MobStore store(Graph behaviour) {
        CustomMob mob = Mobs.zombie("runner", behaviour);
        MobPackage mobPackage = new MobPackage(
                StoreFiles.FORMAT, "runner", "classic", Map.of("en", "Runner"), Map.of(), List.of(), mob, TABLES);
        return new MobStore(new Store(
                "test", List.of(new StorePack("classic", Map.of(), Map.of())), List.of(StoreEntry.of(mobPackage))));
    }

    private static CustomMob only(MobStore.Change change) {
        assertEquals(1, change.mobs().size());
        return change.mobs().getFirst();
    }

    @Test
    void installedMobsAreCurrentUntilTheStoreChanges() {
        CustomMob installed = only(store(OLD).install(List.of(), Set.of(), "classic"));

        assertEquals(StoreState.CURRENT, store(OLD).state(installed));
        assertEquals(StoreState.OUTDATED, store(NEW).state(installed));
        assertEquals(StoreState.OWN, store(OLD).state(Mobs.zombie("mine", OLD)));
    }

    @Test
    void aMobThatWentThroughTheEditorIsStillUnchanged() {
        Graph decimals = Graph.builder()
                .node("wait", NodeInstance.of("flow.delay").literal("duration", DoubleNode.valueOf(60.0)))
                .build();
        CustomMob installed = only(store(decimals).install(List.of(), Set.of(), "classic"));
        String json = EditorPayload.MAPPER.writeValueAsString(installed);
        CustomMob edited = EditorPayload.MAPPER.readValue(json.replace("60.0", "60"), CustomMob.class);

        assertTrue(json.contains("60.0"));
        assertEquals(StoreState.CURRENT, store(decimals).state(edited));
    }

    @Test
    void installAddsOnlyTheTablesTheServerLacks() {
        MobStore.Change fresh = store(OLD).install(List.of(), Set.of(), "runner");
        MobStore.Change kept = store(OLD).install(List.of(), Set.of("runner_loot"), "runner");

        assertEquals(TABLES, fresh.tables());
        assertEquals(Map.of("runner_bones", TABLES.get("runner_bones")), kept.tables());
    }

    @Test
    void installSkipsTakenIds() {
        MobStore.Change change = store(OLD).install(List.of(Mobs.zombie("runner", EDITED)), Set.of(), "runner");

        assertEquals(List.of(), change.done());
        assertEquals(List.of("runner"), change.skipped());
    }

    @Test
    void untouchedMobsUpdateOnTheirOwnAndKeepTheirTuning() {
        CustomMob installed = only(store(OLD).install(List.of(), Set.of(), "classic"));
        CustomMob tuned = new CustomMob(
                installed.id(),
                installed.equipment(),
                installed.attributes(),
                installed.behaviour(),
                installed.loot(),
                installed.extension(),
                new SpawnRules(
                        99,
                        installed.spawn().nightOnly(),
                        installed.spawn().nightEnd(),
                        installed.spawn().reasons(),
                        installed.spawn().conditions()),
                installed.origin());

        CustomMob updated = only(store(NEW).autoUpdate(List.of(tuned)));

        assertEquals(NEW, updated.behaviour());
        assertEquals(99, updated.spawn().weight());
        assertEquals(StoreState.CURRENT, store(NEW).state(updated));
    }

    @Test
    void changedMobsOnlyUpdateWhenAsked() {
        CustomMob installed = only(store(OLD).install(List.of(), Set.of(), "classic"));
        CustomMob edited = installed.withLogic(EDITED, installed.extension(), installed.movement());
        MobStore newer = store(NEW);

        assertEquals(StoreState.CHANGED_OUTDATED, newer.state(edited));
        assertEquals(List.of(), newer.autoUpdate(List.of(edited)).done());
        assertEquals(NEW, only(newer.update(List.of(edited), mob -> true)).behaviour());
    }
}
