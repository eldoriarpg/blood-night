/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * A typed data input or output of a node.
 *
 * @param id       stable id, unique among the node's inputs or outputs
 * @param name     name shown in the editor
 * @param type     Java type of the value
 * @param optional    whether an input may stay unconnected; always false for outputs
 * @param description help text shown in the editor, possibly empty
 * @param group       the part of a large node the port belongs to, e.g. the type declaring a property; null for none
 */
public record Port(
        String id,
        String name,
        PortType type,
        boolean optional,
        String description,
        @Nullable String group) {
    public Port {
        Ids.check(id, "port");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        description = description == null ? "" : description;
    }

    public Port(String id, String name, PortType type, boolean optional, String description) {
        this(id, name, type, optional, description, null);
    }

    public Port(String id, String name, PortType type, boolean optional) {
        this(id, name, type, optional, "");
    }
}
