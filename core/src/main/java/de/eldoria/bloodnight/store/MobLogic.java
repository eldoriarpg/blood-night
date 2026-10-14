/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.editor.EditorPayload;
import de.eldoria.bloodnight.mob.CustomMob;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * The part of a mob a store owns: its behaviour and its extension, which the behaviour is typed by. Name, attributes,
 * equipment, drops and spawn rules are the server's tuning and stay when a store updates a mob.
 */
public final class MobLogic {
    private MobLogic() {}

    /**
     * @return the SHA-256 of the mob's logic as canonical JSON, map entries sorted by key
     */
    public static String hash(CustomMob mob) {
        byte[] json = EditorPayload.MAPPER.writeValueAsBytes(
                Map.of("behaviour", mob.behaviour(), "extension", mob.extension()));
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
