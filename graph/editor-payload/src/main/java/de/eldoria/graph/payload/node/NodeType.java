/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.payload.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A node the editor can place.
 *
 * @param id          stable id stored in graphs
 * @param kind        {@code value}, {@code pure}, {@code action}, {@code control_flow} or {@code entry}
 * @param category    editor category
 * @param name        editor name
 * @param description help text
 * @param macroOnly   only placeable while editing a macro: its boundary nodes
 * @param inputs      data inputs
 * @param outputs     data outputs
 * @param executions  execution outputs
 * @param config      values set on the node
 * @param family      for a variant of another node: that node's id; the editor offers only that node and switches
 *                    between its variants, e.g. the read and change nodes of each type behind one "Mutate"
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record NodeType(
        String id,
        String kind,
        String category,
        String name,
        String description,
        boolean macroOnly,
        List<PortInfo> inputs,
        List<PortInfo> outputs,
        List<ExecutionInfo> executions,
        List<ConfigInfo> config,
        @Nullable String family) {
    /**
     * A node that is no variant of another.
     */
    public NodeType(
            String id,
            String kind,
            String category,
            String name,
            String description,
            boolean macroOnly,
            List<PortInfo> inputs,
            List<PortInfo> outputs,
            List<ExecutionInfo> executions,
            List<ConfigInfo> config) {
        this(id, kind, category, name, description, macroOnly, inputs, outputs, executions, config, null);
    }
}
