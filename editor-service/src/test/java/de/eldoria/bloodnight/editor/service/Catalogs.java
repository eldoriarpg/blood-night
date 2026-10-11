/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.service;

import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.payload.ConfigInfo;
import de.eldoria.graph.payload.ExecutionInfo;
import de.eldoria.graph.payload.NodeType;
import de.eldoria.graph.payload.PortInfo;
import de.eldoria.graph.payload.TypeRef;

import java.util.List;
import java.util.Map;

/**
 * A small catalog: an entry with a chain, an action with an input and a config field, a value node.
 */
final class Catalogs {
    private static final TypeRef NUMBER = new TypeRef("number", null, null, List.of());

    private Catalogs() {}

    static Catalog small() {
        return new Catalog(
                Catalog.FORMAT,
                "test-catalog",
                List.of(
                        node(
                                "mob.tick",
                                List.of(),
                                List.of(),
                                List.of(new ExecutionInfo("next", "Next", false)),
                                List.of()),
                        node(
                                "entity.burn",
                                List.of(new PortInfo("ticks", "Ticks", "", NUMBER, false, true)),
                                List.of(),
                                List.of(new ExecutionInfo("next", "Next", false)),
                                List.of(new ConfigInfo("mode", "Mode", NUMBER, null))),
                        node(
                                "value.number",
                                List.of(),
                                List.of(new PortInfo("value", "Value", "", NUMBER, false, false)),
                                List.of(),
                                List.of())),
                List.of(),
                Map.of());
    }

    private static NodeType node(
            String id,
            List<PortInfo> inputs,
            List<PortInfo> outputs,
            List<ExecutionInfo> executions,
            List<ConfigInfo> config) {
        return new NodeType(id, "ACTION", "test", id, "", false, inputs, outputs, executions, config);
    }
}
