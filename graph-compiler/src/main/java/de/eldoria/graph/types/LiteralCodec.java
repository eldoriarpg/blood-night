/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.types;

import com.palantir.javapoet.CodeBlock;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * Turns a config value from the graph into a Java literal.
 * <p>
 * This is the only path by which data from a graph reaches generated code. {@link #read} must reject anything that is
 * not a valid value of the type, and {@link #emit} must produce a self-contained expression built from JavaPoet
 * placeholders ({@code $S}, {@code $T}, {@code $N}) or from values that are not strings.
 *
 * @param <T> Java type of the value
 */
public interface LiteralCodec<T> {
    /**
     * @return the class this codec reads and emits; primitive codecs also serve the wrapper type
     */
    Class<?> type();

    /**
     * @param node value from the graph
     * @return the value
     * @throws IllegalArgumentException when the node is not a valid value of the type
     */
    T read(JsonNode node);

    /**
     * @param value a value returned by {@link #read}
     * @return an expression evaluating to the value
     */
    CodeBlock emit(T value);

    /**
     * Values the editor offers in a dropdown, e.g. enum constants or registry keys. Empty when any value of the type
     * is fine.
     *
     * @return the allowed values as they are written into the graph
     */
    default List<String> values() {
        return List.of();
    }

    /**
     * How the editor lets users type a value in, when a plain text field does not fit, e.g. {@code color} for a
     * colour picker writing {@code #rrggbb}.
     *
     * @return the input format, or null for the input the value's shape implies
     */
    default @Nullable String format() {
        return null;
    }

    /**
     * Reads and emits in one step.
     *
     * @param node value from the graph
     * @return an expression evaluating to the value
     */
    default CodeBlock literal(JsonNode node) {
        return emit(read(node));
    }
}
