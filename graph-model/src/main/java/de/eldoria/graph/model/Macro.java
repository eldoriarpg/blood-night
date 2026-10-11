/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.model;

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
        name = name == null ? id : name;
        description = description == null ? "" : description;
        category = category == null ? "macro" : category;
        Objects.requireNonNull(body, "body");
    }
}
