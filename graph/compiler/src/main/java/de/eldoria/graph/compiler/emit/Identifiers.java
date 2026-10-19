/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler.emit;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.lang.model.SourceVersion;

/**
 * Hands out Java identifiers that are unique within one namespace, such as the locals of a method or the fields of a
 * class. A wanted name is used as it is while free; a clash appends 2, 3, ... Everything outside
 * {@code [A-Za-z0-9]} is dropped, so graph ids cannot alter the code, and keywords get a leading underscore.
 */
final class Identifiers {
    private final Set<String> used = new HashSet<>();

    /**
     * @param reserved names that are taken already, e.g. method parameters
     */
    Identifiers(String... reserved) {
        used.addAll(Arrays.asList(reserved));
    }

    /**
     * @param name a name already in Java form
     * @return whether it is taken
     */
    boolean taken(String name) {
        return used.contains(name);
    }

    /**
     * Takes a name, or the first free variant of it.
     *
     * @param name a name already in Java form
     * @return the name taken
     */
    String claim(String name) {
        String claimed = first(name, candidate -> !used.contains(candidate));
        used.add(claimed);
        return claimed;
    }

    /**
     * @param name a name already in Java form
     * @param free whether a candidate may be used
     * @return the name, or its first free variant
     */
    static String first(String name, Predicate<String> free) {
        String base = name.isEmpty() ? "value" : name;
        if (SourceVersion.isKeyword(base) || !SourceVersion.isIdentifier(base)) base = "_" + base;
        if (free.test(base)) return base;
        for (int i = 2; ; i++) {
            if (free.test(base + i)) return base + i;
        }
    }

    /**
     * @param text words separated by anything but letters and digits, e.g. {@code target_location}
     * @return the words as {@code lowerCamelCase}
     */
    static String lowerCamel(String text) {
        String[] words = words(text);
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.isEmpty()) {
                result.append(word.toLowerCase(Locale.ROOT));
            } else {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return result.toString();
    }

    /**
     * @param text words separated by anything but letters and digits, e.g. {@code minecraft:speed}
     * @return the words as {@code UPPER_SNAKE_CASE}
     */
    static String upperSnake(String text) {
        return Arrays.stream(words(text))
                .map(word -> word.toUpperCase(Locale.ROOT))
                .collect(Collectors.joining("_"));
    }

    private static String[] words(String text) {
        String spaced = text.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
        return Arrays.stream(spaced.split("[^A-Za-z0-9]+"))
                .filter(word -> !word.isEmpty())
                .toArray(String[]::new);
    }
}
