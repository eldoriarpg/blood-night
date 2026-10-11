/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.payload;

import com.fasterxml.jackson.annotation.JsonInclude;

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
        List<ConfigInfo> config) {}
