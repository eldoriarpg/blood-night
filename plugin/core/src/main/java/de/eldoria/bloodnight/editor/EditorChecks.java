/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.editor.payload.SettingsPayload;
import de.eldoria.bloodnight.graph.BloodNightNodes;
import de.eldoria.bloodnight.loot.LootEntry;
import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.loot.assign.ServerLoot;
import de.eldoria.bloodnight.loot.assign.WorldLoot;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.MobParticles;
import de.eldoria.bloodnight.mob.MobTypes;
import de.eldoria.bloodnight.mob.equipment.Equipment;
import de.eldoria.bloodnight.particles.BuiltInEffects;
import de.eldoria.bloodnight.particles.EffectChecks;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.Macro;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Checks a result from the editor against the server before it is applied: each mob is stored under its own id, mobs
 * and their extensions are entity types a mob can be ({@link MobTypes}), mobs and loot tables only use registered items
 * or item materials, mobs, worlds and the server only assign loot tables the result holds, graphs only play particle
 * effects that exist, the result's own particle effects pass {@link EffectChecks}, and the files it replaces did not
 * change since the session opened.
 * Worlds the server does not know are reported separately; they are skipped.
 */
public final class EditorChecks {
    private EditorChecks() {}

    /**
     * @param result the result
     * @param items  whether the server knows an item id
     * @return why the result cannot be applied; empty when it can
     */
    public static List<String> problems(EditorResult result, Predicate<String> items) {
        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, CustomMob> entry : result.mobs().entrySet()) {
            CustomMob mob = entry.getValue();
            if (!entry.getKey().equals(mob.id())) {
                problems.add("Mob %s is stored as %s".formatted(mob.id(), entry.getKey()));
            }
            EntityType type = mob.attributes().entityType();
            if (type != null && !MobTypes.isMob(type)) {
                problems.add("Mob %s cannot be a %s, which is no mob".formatted(mob.id(), type.name()));
            }
            EntityType extension = mob.extension().entityType();
            if (extension != null && !MobTypes.isMob(extension)) {
                problems.add("The extension of mob %s cannot be a %s, which is no mob"
                        .formatted(mob.id(), extension.name()));
            }
            for (String item : usedItems(mob)) {
                if (!knows(result, items, item)) {
                    problems.add("Mob %s uses unknown item %s".formatted(mob.id(), item));
                }
            }
            unknownTables(result, mob.loot().tables())
                    .forEach(table -> problems.add("Mob %s uses unknown loot table %s".formatted(mob.id(), table)));
            List<String> effects = new ArrayList<>(effectsOf(mob.behaviour()));
            MobParticles looks = mob.particles();
            if (looks != null) {
                Stream.of(looks.ambient(), looks.spawn(), looks.death())
                        .filter(Objects::nonNull)
                        .forEach(effects::add);
            }
            unknownEffects(result, effects)
                    .forEach(effect ->
                            problems.add("Mob %s uses unknown particle effect %s".formatted(mob.id(), effect)));
        }
        for (Macro macro : result.macros()) {
            unknownEffects(result, effectsOf(macro.body()))
                    .forEach(effect ->
                            problems.add("Macro %s uses unknown particle effect %s".formatted(macro.id(), effect)));
        }
        result.particleEffects().forEach((id, effect) -> {
            if (BuiltInEffects.builtIn(id)) {
                problems.add("Particle effect %s cannot use the built-in namespace".formatted(id));
            }
            problems.addAll(EffectChecks.problems(id, effect, item -> knows(result, items, item)));
        });
        for (Map.Entry<String, LootTable> table : result.lootTables().entrySet()) {
            table.getValue().entries().stream()
                    .map(LootEntry::item)
                    .distinct()
                    .filter(item -> !knows(result, items, item))
                    .forEach(
                            item -> problems.add("Loot table %s uses unknown item %s".formatted(table.getKey(), item)));
        }
        SettingsPayload settings = result.settings();
        if (settings != null) {
            ServerLoot server = settings.main().loot();
            List<String> assigned = new ArrayList<>(server.tables());
            assigned.addAll(server.ordinary().tables());
            unknownTables(result, assigned)
                    .forEach(table -> problems.add("The server uses unknown loot table %s".formatted(table)));
            settings.worlds().values().forEach(world -> {
                WorldLoot loot = world.settings().loot();
                List<String> tables = new ArrayList<>(loot.tables());
                tables.addAll(loot.ordinary().tables());
                unknownTables(result, tables)
                        .forEach(table ->
                                problems.add("World %s uses unknown loot table %s".formatted(world.name(), table)));
            });
        }
        return problems;
    }

    private static boolean knows(EditorResult result, Predicate<String> items, String item) {
        return items.test(item) || result.items().containsKey(item) || isMaterial(item);
    }

    /**
     * @return the ids of the particle effects a graph plays
     */
    static List<String> effectsOf(Graph graph) {
        return graph.nodes().values().stream()
                .filter(node -> node.type().equals(BloodNightNodes.WORLD_PLAY_EFFECT))
                .map(node -> node.config().get("effect"))
                .filter(value -> value != null && value.isString())
                .map(JsonNode::asString)
                .toList();
    }

    private static List<String> unknownEffects(EditorResult result, List<String> effects) {
        return effects.stream()
                .distinct()
                .filter(effect -> !BuiltInEffects.all().containsKey(effect)
                        && !result.particleEffects().containsKey(effect))
                .toList();
    }

    private static List<String> unknownTables(EditorResult result, List<String> tables) {
        return tables.stream()
                .distinct()
                .filter(table -> !result.lootTables().containsKey(table))
                .toList();
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

    /**
     * @param result  the result
     * @param current the hashes of the files now, see {@code Configuration.fileHashes}
     * @return the files that changed since the session opened, sorted; every file when the result has no hashes
     */
    public static List<String> changedFiles(EditorResult result, Map<String, String> current) {
        return changedFiles(result.hashes(), current);
    }

    /**
     * @param opened  the hashes of the files when the session opened
     * @param current the hashes of the files now
     * @return the files that changed, sorted; every file when no hashes were opened with
     */
    public static List<String> changedFiles(Map<String, String> opened, Map<String, String> current) {
        return current.entrySet().stream()
                .filter(file -> !file.getValue().equals(opened.get(file.getKey())))
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
    }

    /**
     * @param payload a session's payload
     * @return the file hashes it carries, empty when it lost them
     */
    public static Map<String, String> hashes(JsonNode payload) {
        Map<String, String> hashes = new HashMap<>();
        for (var entry : payload.path("hashes").properties()) {
            if (entry.getValue().isString())
                hashes.put(entry.getKey(), entry.getValue().asString());
        }
        return hashes;
    }

    private static boolean isMaterial(String id) {
        Material material = Material.matchMaterial(id);
        return material != null && material.isItem();
    }

    private static List<String> usedItems(CustomMob mob) {
        List<String> items = new ArrayList<>(slots(mob.equipment()));
        items.addAll(slots(mob.extension().equipment()));
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
