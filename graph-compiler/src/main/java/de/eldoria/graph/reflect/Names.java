package de.eldoria.graph.reflect;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Turns ids into editor names: {@code final_damage} → {@code Final Damage}.
 */
final class Names {
    private Names() {
    }

    static String humanize(String id) {
        return Arrays.stream(id.split("_"))
                .filter(word -> !word.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
