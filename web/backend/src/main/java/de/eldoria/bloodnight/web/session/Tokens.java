/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.session;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Session ids and pull tokens: random, URL safe; tokens are stored only as their SHA-256 hash.
 */
public final class Tokens {
    private static final SecureRandom RANDOM = new SecureRandom();

    private Tokens() {}

    /**
     * @return a session id, 128 random bits
     */
    public static String sessionId() {
        return random(16);
    }

    /**
     * @return a pull token, 256 random bits
     */
    public static String pullToken() {
        return random(32);
    }

    /**
     * @param token a pull token
     * @return its hash as stored
     */
    public static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @param token a pull token as sent
     * @param hash  the stored hash
     * @return whether the token is the one the hash was made of, compared in constant time
     */
    public static boolean matches(String token, String hash) {
        return MessageDigest.isEqual(
                hash(token).getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
    }

    private static String random(int bytes) {
        byte[] value = new byte[bytes];
        RANDOM.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
