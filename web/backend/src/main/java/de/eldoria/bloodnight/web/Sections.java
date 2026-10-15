/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Finds the sections of the payload a change touches: {@code mobs/<id>}, {@code macros/<id>}, {@code items/<id>}, {@code settings/main},
 * {@code lootTables/<id>} and {@code settings/worlds/<uid>}.
 * <p>
 * Macros are a list, so a pointer into it names the macro at that index as the document stands when the operation
 * runs, or the added macro for an {@code add}. A pointer above section level, such as the whole payload or
 * {@code /mobs}, touches everything. The server's choices, hashes, the catalog version and the problem lists cannot be changed.
 */
final class Sections {
    private static final Set<String> READ_ONLY = Set.of(
            "choices", "hashes", "catalog", "problems", "macroProblems", "configProblems", "store", "setup", "legacy");

    private Sections() {}

    /**
     * What a change touches.
     *
     * @param sections   the sections
     * @param everything whether it touches the payload above section level
     */
    record Touched(Set<String> sections, boolean everything) {}

    /**
     * @param document   the payload before the change
     * @param operations the change's operations
     * @return what they touch
     * @throws JsonPatch.PatchException when an operation is malformed, does not fit or changes something read-only
     */
    static Touched of(JsonNode document, JsonNode operations) {
        if (!operations.isArray()) throw new JsonPatch.PatchException("The operations are no list");
        Set<String> sections = new LinkedHashSet<>();
        boolean everything = false;
        JsonNode current = document;
        for (JsonNode operation : operations) {
            List<String> targets = new ArrayList<>();
            targets.add(operation.path("path").asString(""));
            if (operation.has("from") && operation.path("op").asString("").equals("move")) {
                targets.add(operation.path("from").asString(""));
            }
            for (String target : targets) {
                String section = section(current, JsonPatch.pointer(target), operation);
                if (section == null) everything = true;
                else sections.add(section);
            }
            current =
                    JsonPatch.apply(current, EditorApp.MAPPER.createArrayNode().add(operation));
        }
        return new Touched(sections, everything);
    }

    /**
     * @return the section, or null for everything
     */
    private static @Nullable String section(JsonNode document, List<String> path, JsonNode operation) {
        if (path.isEmpty()) return null;
        String root = path.getFirst();
        if (READ_ONLY.contains(root)) throw new JsonPatch.PatchException(root + " cannot be changed in the editor");
        return switch (root) {
            case "mobs" -> path.size() < 2 ? null : "mobs/" + path.get(1);
            case "items" -> path.size() < 2 ? null : "items/" + path.get(1);
            case "lootTables" -> path.size() < 2 ? null : "lootTables/" + path.get(1);
            case "macros" -> path.size() < 2 ? null : "macros/" + macroId(document, path, operation);
            case "settings" -> settings(path);
            default -> throw new JsonPatch.PatchException("Unknown part " + root);
        };
    }

    private static String macroId(JsonNode document, List<String> path, JsonNode operation) {
        boolean addsMacro =
                path.size() == 2 && operation.path("op").asString("").equals("add");
        JsonNode macro = addsMacro ? operation.path("value") : indexed(document.path("macros"), path.get(1));
        String id = macro.path("id").asString("");
        if (id.isEmpty()) throw new JsonPatch.PatchException("The macro has no id");
        return id;
    }

    private static JsonNode indexed(JsonNode list, String index) {
        try {
            JsonNode node = list.get(Integer.parseInt(index));
            if (node == null) throw new JsonPatch.PatchException("No macro at " + index);
            return node;
        } catch (NumberFormatException e) {
            throw new JsonPatch.PatchException("Bad list index " + index);
        }
    }

    private static @Nullable String settings(List<String> path) {
        if (path.size() < 2) return null;
        return switch (path.get(1)) {
            case "main" -> "settings/main";
            case "worlds" -> path.size() < 3 ? null : "settings/worlds/" + path.get(2);
            default -> throw new JsonPatch.PatchException("Unknown settings " + path.get(1));
        };
    }
}
