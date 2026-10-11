/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A reusable subgraph defined by users and shared across graphs.
 * <p>
 * Its body is an ordinary graph whose boundary nodes ({@code macro.input.*}, {@code macro.output},
 * {@code macro.start}, {@code macro.end}) define the ports the macro shows when used as a node of type
 * {@code macro.<id>}. Graphs store only that use node; the body is spliced in when compiling.
 *
 * @param id          stable id, a lower-case segment such as {@code knockback}
 * @param name        name shown in the editor
 * @param description help text shown in the editor
 * @param category    editor category
 * @param body        the subgraph
 */
public record Macro(String id, String name, String description, String category, Graph body) {
    public Macro {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(body, "body");
    }

    /**
     * Creates a macro named after its id, without description, in the {@code macro} category.
     *
     * @param id   stable id
     * @param body the subgraph
     * @return the macro
     */
    public static Macro of(String id, Graph body) {
        return read(id, null, null, null, body);
    }

    /**
     * Reads a macro, filling in what the JSON leaves out: the name defaults to the id, the description to empty, the
     * category to {@code macro}.
     */
    @JsonCreator
    static Macro read(
            @JsonProperty("id") String id,
            @JsonProperty("name") @Nullable String name,
            @JsonProperty("description") @Nullable String description,
            @JsonProperty("category") @Nullable String category,
            @JsonProperty("body") Graph body) {
        return new Macro(
                id,
                name == null ? id : name,
                description == null ? "" : description,
                category == null ? "macro" : category,
                body);
    }
}
