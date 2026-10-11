package de.eldoria.graph.reflect;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Naming rules shared by derived nodes: Java names become ids, ids become editor names.
 */
public final class Names {
    private static final Set<String> KEYWORDS = Set.of("abstract", "assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
            "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
            "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false",
            "null", "var", "yield", "record", "sealed", "permits");

    private Names() {
    }

    /**
     * Turns a Java name into a port id: {@code FinalDamage} → {@code final_damage}, {@code UUIDString} →
     * {@code uuid_string}. Java keywords get a trailing underscore, so ids double as variable names.
     */
    public static String snakeCase(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char current = name.charAt(i);
            if (Character.isUpperCase(current) && i > 0) {
                char previous = name.charAt(i - 1);
                boolean nextIsLower = i + 1 < name.length() && Character.isLowerCase(name.charAt(i + 1));
                if (Character.isLowerCase(previous) || Character.isDigit(previous) || (Character.isUpperCase(previous) && nextIsLower)) {
                    result.append('_');
                }
            }
            result.append(Character.toLowerCase(current));
        }
        String id = result.toString().replaceAll("[^a-z0-9_]", "").replaceAll("^[^a-z]+", "");
        if (id.isEmpty()) throw new IllegalArgumentException("Cannot derive an id from " + name);
        return KEYWORDS.contains(id) ? id + "_" : id;
    }

    /**
     * Turns an id into an editor name: {@code final_damage} → {@code Final Damage}.
     */
    public static String humanize(String id) {
        return Arrays.stream(id.split("_"))
                .filter(word -> !word.isEmpty())
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
