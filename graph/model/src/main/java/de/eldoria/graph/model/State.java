/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A state of a graph: while the graph is in it, the entries of the nodes that belong to it run. States nest; a state
 * inside another is only active together with it.
 *
 * @param name    the name, unique within the graph
 * @param parent  the name of the state it lies in, or {@code null} for a top-level state
 * @param initial whether it is the state entered first among its siblings
 */
public record State(
        String name,
        @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable String parent,
        @JsonInclude(JsonInclude.Include.NON_DEFAULT) boolean initial) {
    public State {
        Objects.requireNonNull(name, "name");
        if (parent != null && parent.isBlank()) parent = null;
    }
}
