/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ContainerNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON Patch (RFC 6902) on Jackson trees: {@code add}, {@code remove}, {@code replace}, {@code move}, {@code copy} and
 * {@code test}, with JSON pointers (RFC 6901). A patch applies to a copy, all operations or none.
 */
final class JsonPatch {
    private JsonPatch() {}

    /**
     * Thrown when a patch is malformed or does not fit the document.
     */
    static final class PatchException extends RuntimeException {
        PatchException(String message) {
            super(message);
        }
    }

    /**
     * @param document   the document, left unchanged
     * @param operations the operations, a JSON array
     * @return the patched copy
     * @throws PatchException when an operation is malformed or does not fit
     */
    static JsonNode apply(JsonNode document, JsonNode operations) {
        if (!operations.isArray()) throw new PatchException("The operations are no list");
        JsonNode result = document.deepCopy();
        for (int i = 0; i < operations.size(); i++) {
            try {
                result = applyOne(result, operations.get(i));
            } catch (PatchException e) {
                throw new PatchException("Operation %d: %s".formatted(i, e.getMessage()));
            }
        }
        return result;
    }

    private static JsonNode applyOne(JsonNode document, JsonNode operation) {
        String op = operation.path("op").asString("");
        List<String> path = pointer(text(operation, "path"));
        return switch (op) {
            case "add" -> add(document, path, value(operation));
            case "remove" -> remove(document, path);
            case "replace" -> {
                if (path.isEmpty()) yield value(operation);
                get(document, path);
                yield add(remove(document, path), path, value(operation));
            }
            case "move" -> {
                List<String> from = pointer(text(operation, "from"));
                if (path.size() > from.size() && path.subList(0, from.size()).equals(from)) {
                    throw new PatchException("Cannot move a value into itself");
                }
                JsonNode value = get(document, from);
                yield add(remove(document, from), path, value);
            }
            case "copy" ->
                add(
                        document,
                        path,
                        get(document, pointer(text(operation, "from"))).deepCopy());
            case "test" -> {
                if (!get(document, path).equals(value(operation))) {
                    throw new PatchException("Test failed at " + text(operation, "path"));
                }
                yield document;
            }
            default -> throw new PatchException("Unknown operation " + op);
        };
    }

    private static JsonNode add(JsonNode document, List<String> path, JsonNode value) {
        if (path.isEmpty()) return value;
        ContainerNode<?> parent = container(get(document, path.subList(0, path.size() - 1)));
        String key = path.getLast();
        if (parent instanceof ObjectNode object) {
            object.set(key, value);
        } else {
            ArrayNode array = (ArrayNode) parent;
            if (key.equals("-")) array.add(value);
            else array.insert(index(key, array.size()), value);
        }
        return document;
    }

    private static JsonNode remove(JsonNode document, List<String> path) {
        if (path.isEmpty()) throw new PatchException("Cannot remove the whole document");
        ContainerNode<?> parent = container(get(document, path.subList(0, path.size() - 1)));
        String key = path.getLast();
        if (parent instanceof ObjectNode object) {
            if (object.remove(key) == null) throw new PatchException("Nothing at " + key);
        } else {
            ArrayNode array = (ArrayNode) parent;
            array.remove(index(key, array.size() - 1));
        }
        return document;
    }

    private static JsonNode get(JsonNode document, List<String> path) {
        JsonNode node = document;
        for (String key : path) {
            node = node.isArray() ? node.get(index(key, node.size() - 1)) : node.get(key);
            if (node == null) throw new PatchException("Nothing at " + key);
        }
        return node;
    }

    private static ContainerNode<?> container(JsonNode node) {
        if (node instanceof ContainerNode<?> container) return container;
        throw new PatchException("Not an object or list");
    }

    private static int index(String key, int max) {
        try {
            int index = Integer.parseInt(key);
            if (index < 0 || index > max || (key.length() > 1 && key.startsWith("0")))
                throw new NumberFormatException();
            return index;
        } catch (NumberFormatException e) {
            throw new PatchException("Bad list index " + key);
        }
    }

    private static JsonNode value(JsonNode operation) {
        JsonNode value = operation.get("value");
        if (value == null) throw new PatchException("Missing value");
        return value.deepCopy();
    }

    private static String text(JsonNode operation, String field) {
        JsonNode value = operation.get(field);
        if (value == null || !value.isString()) throw new PatchException("Missing " + field);
        return value.asString();
    }

    /**
     * @param pointer a JSON pointer, e.g. {@code /mobs/a~1b/0}
     * @return its unescaped segments
     */
    static List<String> pointer(String pointer) {
        if (pointer.isEmpty()) return List.of();
        if (!pointer.startsWith("/")) throw new PatchException("Bad pointer " + pointer);
        List<String> segments = new ArrayList<>();
        for (String segment : pointer.substring(1).split("/", -1)) {
            segments.add(segment.replace("~1", "/").replace("~0", "~"));
        }
        return segments;
    }
}
