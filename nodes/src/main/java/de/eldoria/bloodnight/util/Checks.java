package de.eldoria.bloodnight.util;

import de.eldoria.bloodnight.nodes.meta.DataType;
import org.jetbrains.annotations.Contract;

import java.util.Locale;
import java.util.Objects;

public class Checks {
    @Contract("null, _ -> fail")
    public static void notNull(Object object, String message) {
        Objects.requireNonNull(object, message);
    }

    public static void isSubType(DataType type1, DataType type2) {
        if (!type1.isType(type2)) {
            throw new IllegalStateException("Data type is %s but %s was expected.".formatted(type1, type2));
        }
    }

    @Contract("null, _ -> fail")
    public static void lowerCase(String string, String message) {
        if (string.toLowerCase(Locale.ROOT).equals(string)) return;
        throw new IllegalStateException(message);
    }
}
