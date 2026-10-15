/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.api.MovementPreset;
import de.eldoria.bloodnight.editor.EditorPayload;
import de.eldoria.bloodnight.mob.CustomMob;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The part of a mob a store owns: its behaviour, its extension, which the behaviour is typed by, and its movement.
 * Name, attributes, equipment, drops and spawn rules are the server's tuning and stay when a store updates a mob.
 */
public final class MobLogic {
    private MobLogic() {}

    /**
     * @return the SHA-256 of the mob's logic as canonical JSON: object keys sorted at every level and numbers by value,
     *     so a graph that went through the editor, which writes {@code 60.0} as {@code 60}, keeps its hash. A vanilla
     *     movement is left out, so mobs without one keep the hash they had before movements existed.
     */
    public static String hash(CustomMob mob) {
        Map<String, Object> logic = new TreeMap<>(Map.of("behaviour", mob.behaviour(), "extension", mob.extension()));
        if (mob.movement().preset() != MovementPreset.VANILLA) logic.put("movement", mob.movement());
        JsonNode canonical = canonical(EditorPayload.MAPPER.valueToTree(logic));
        byte[] json = EditorPayload.MAPPER.writeValueAsBytes(canonical);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @return the node with object keys sorted and every number as the decimal of its value without trailing zeros
     */
    static JsonNode canonical(JsonNode node) {
        JsonNodeFactory nodes = JsonNodeFactory.instance;
        if (node.isNumber()) return nodes.numberNode(node.decimalValue().stripTrailingZeros());
        if (node.isArray()) {
            ArrayNode array = nodes.arrayNode();
            node.forEach(element -> array.add(canonical(element)));
            return array;
        }
        if (node.isObject()) {
            ObjectNode object = nodes.objectNode();
            new TreeSet<>(node.propertyNames()).forEach(name -> object.set(name, canonical(node.get(name))));
            return object;
        }
        return node;
    }
}
