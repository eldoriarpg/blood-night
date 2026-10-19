/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition.descriptor;

import java.util.regex.Pattern;

/**
 * Rules for stable ids. Node ids are dotted lower-case paths ({@code math.add}); port and field ids are single
 * segments ({@code result}).
 */
public final class Ids {
    private static final Pattern SEGMENT = Pattern.compile("[a-z][a-z0-9_]*");
    private static final Pattern PATH = Pattern.compile("[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*");

    private Ids() {}

    public static String check(String id, String what) {
        if (id == null || !SEGMENT.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid %s id '%s', expected %s".formatted(what, id, SEGMENT));
        }
        return id;
    }

    static String checkPath(String id, String what) {
        if (id == null || !PATH.matcher(id).matches()) {
            throw new IllegalArgumentException("Invalid %s id '%s', expected %s".formatted(what, id, PATH));
        }
        return id;
    }

    /**
     * Turns {@code math.add_all} into {@code Add All}.
     */
    static String humanize(String id) {
        String last = id.substring(id.lastIndexOf('.') + 1);
        StringBuilder result = new StringBuilder();
        for (String word : last.split("_")) {
            if (word.isEmpty()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }
}
