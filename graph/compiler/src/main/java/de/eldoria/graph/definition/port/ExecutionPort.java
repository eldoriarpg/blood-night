/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition.port;

import de.eldoria.graph.definition.descriptor.Ids;

import java.util.Objects;

/**
 * An execution output: a slot nodes are chained into.
 *
 * @param id        stable id, unique among the node's execution ports
 * @param name      name shown in the editor
 * @param repeating whether the chained code may run several times per invocation, as in a loop body; values computed
 *                  outside are then not reused inside, so every iteration sees fresh ones
 */
public record ExecutionPort(String id, String name, boolean repeating) {
    public ExecutionPort {
        Ids.check(id, "execution port");
        Objects.requireNonNull(name, "name");
    }
}
