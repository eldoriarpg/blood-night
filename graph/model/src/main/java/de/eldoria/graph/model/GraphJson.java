/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.model;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Reads and writes graphs as JSON. Output is deterministic: nodes, ports and config fields are ordered by id.
 */
public final class GraphJson {
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private GraphJson() {}

    /**
     * @return the mapper used for graphs, for embedding graphs into larger documents
     */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /**
     * Parses a graph.
     *
     * @param json graph JSON
     * @return the graph
     * @throws JacksonException when the JSON is malformed or does not describe a graph
     */
    public static Graph read(String json) {
        return MAPPER.readValue(json, Graph.class);
    }

    /**
     * Serializes a graph.
     *
     * @param graph the graph
     * @return compact JSON
     */
    public static String write(Graph graph) {
        return MAPPER.writeValueAsString(graph);
    }

    /**
     * Parses a macro.
     *
     * @param json macro JSON
     * @return the macro
     * @throws JacksonException when the JSON is malformed or does not describe a macro
     */
    public static Macro readMacro(String json) {
        return MAPPER.readValue(json, Macro.class);
    }

    /**
     * Serializes a macro.
     *
     * @param macro the macro
     * @return compact JSON
     */
    public static String writeMacro(Macro macro) {
        return MAPPER.writeValueAsString(macro);
    }

    /**
     * Serializes a graph for humans.
     *
     * @param graph the graph
     * @return indented JSON
     */
    public static String writePretty(Graph graph) {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(graph);
    }
}
