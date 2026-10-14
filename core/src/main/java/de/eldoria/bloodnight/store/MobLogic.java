/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.api.MovementPreset;
import de.eldoria.bloodnight.editor.EditorPayload;
import de.eldoria.bloodnight.mob.CustomMob;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;

/**
 * The part of a mob a store owns: its behaviour, its extension, which the behaviour is typed by, and its movement.
 * Name, attributes, equipment, drops and spawn rules are the server's tuning and stay when a store updates a mob.
 */
public final class MobLogic {
    private MobLogic() {}

    /**
     * @return the SHA-256 of the mob's logic as canonical JSON, map entries sorted by key; a vanilla movement is left
     *     out, so mobs without one keep the hash they had before movements existed
     */
    public static String hash(CustomMob mob) {
        Map<String, Object> logic = new TreeMap<>(Map.of("behaviour", mob.behaviour(), "extension", mob.extension()));
        if (mob.movement().preset() != MovementPreset.VANILLA) logic.put("movement", mob.movement());
        byte[] json = EditorPayload.MAPPER.writeValueAsBytes(logic);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
