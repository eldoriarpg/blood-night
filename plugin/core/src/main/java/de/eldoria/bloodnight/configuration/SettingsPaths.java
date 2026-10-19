/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration;

import de.eldoria.bloodnight.editor.payload.EditorPayload;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads and changes one setting of a settings object by its path, such as {@code nightSettings.skippable}, the way
 * the settings files name it. The object is turned into a tree with the web editor's mapper, changed and read back,
 * so a value is checked exactly like a value from the editor.
 * <p>
 * A typed value is read by what the setting holds: {@code true} or {@code false} for switches, a number for
 * numbers, the text as it is for texts, and JSON for lists, maps and sections, e.g. {@code ["SPAWNER"]}.
 */
public final class SettingsPaths {
    private static final ObjectMapper MAPPER = EditorPayload.MAPPER;

    private SettingsPaths() {}

    /**
     * @return every path of the object down to single settings; lists and empty maps count as one setting, the entries
     *     of a filled map each as their own
     */
    public static List<String> paths(Object settings) {
        List<String> paths = new ArrayList<>();
        collect(MAPPER.valueToTree(settings), "", paths);
        return paths;
    }

    /**
     * @return the setting at the path, if there is one
     */
    public static Optional<JsonNode> read(Object settings, String path) {
        JsonNode node = MAPPER.valueToTree(settings).at(pointer(path));
        return node.isMissingNode() ? Optional.empty() : Optional.of(node);
    }

    /**
     * @param settings the current settings, left unchanged
     * @param type     their type
     * @param path     the setting to change
     * @param value    the new value as typed
     * @return new settings with the change
     * @throws IllegalArgumentException when the path names no setting or the value does not fit it
     */
    public static <T> T write(T settings, Class<T> type, String path, String value) {
        JsonNode tree = MAPPER.valueToTree(settings);
        int split = path.lastIndexOf('.');
        JsonNode parent = split < 0 ? tree : tree.at(pointer(path.substring(0, split)));
        String key = path.substring(split + 1);
        if (!(parent instanceof ObjectNode object) || !object.has(key)) {
            throw new IllegalArgumentException("There is no setting " + path);
        }
        object.set(key, parse(object.get(key), value));
        try {
            return MAPPER.treeToValue(tree, type);
        } catch (JacksonException e) {
            throw new IllegalArgumentException(e.getOriginalMessage(), e);
        }
    }

    private static JsonNode parse(JsonNode current, String value) {
        if (current.isString()) return StringNode.valueOf(value);
        JsonNode parsed;
        try {
            parsed = MAPPER.readTree(value);
        } catch (JacksonException e) {
            if (current.isNull()) return StringNode.valueOf(value);
            throw new IllegalArgumentException("%s is not a valid value here".formatted(value), e);
        }
        boolean fits = current.isNull()
                || (current.isBoolean() && parsed.isBoolean())
                || (current.isNumber() && parsed.isNumber())
                || (current.isContainer() && parsed.isContainer() && current.isArray() == parsed.isArray());
        if (!fits) throw new IllegalArgumentException("%s does not fit here".formatted(value));
        return parsed;
    }

    private static void collect(JsonNode node, String prefix, List<String> paths) {
        if (!(node instanceof ObjectNode object) || object.isEmpty()) {
            if (!prefix.isEmpty()) paths.add(prefix);
            return;
        }
        for (Map.Entry<String, JsonNode> field : object.properties()) {
            collect(field.getValue(), prefix.isEmpty() ? field.getKey() : prefix + "." + field.getKey(), paths);
        }
    }

    private static String pointer(String path) {
        return "/" + path.replace("~", "~0").replace("/", "~1").replace('.', '/');
    }
}
