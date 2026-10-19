/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.platform;

import de.eldoria.graph.definition.port.PortType;

import java.util.List;
import java.util.Objects;

/**
 * A type variable bound for a whole graph rather than by what is wired into a node, such as the type of the object a
 * graph runs for. Node definitions use it like any variable; each compile may bind it, otherwise it is the fallback.
 *
 * @param fallback   the type when a compile binds nothing, e.g. while a macro is checked on its own
 * @param candidates every type a compile may bind, so editors know them; the fallback is one of them implicitly
 */
public record Binding(PortType fallback, List<PortType> candidates) {
    public Binding {
        Objects.requireNonNull(fallback, "fallback");
        candidates = List.copyOf(candidates);
    }
}
