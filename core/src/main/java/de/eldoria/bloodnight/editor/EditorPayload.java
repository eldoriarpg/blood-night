/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.serialization.EditorSerialization;
import de.eldoria.graph.compiler.GraphProblem;
import de.eldoria.graph.model.Macro;
import de.eldoria.jacksonbukkit.JacksonPaper;
import org.bukkit.inventory.ItemStack;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * What the editor gets from a server: the user data, plus the version of the catalog it was made for.
 * <p>
 * The catalog itself (nodes, ports, types) is generated at build time and loaded by the editor on its own; the
 * payload only names it, so the editor can tell when a server runs a different platform.
 *
 * @param catalog        version of the catalog the server runs
 * @param mobs           mob definitions by id, including their behaviour graphs
 * @param macros         the macro library
 * @param items          registered items by id
 * @param problems       problems per mob id
 * @param macroProblems  problems per macro id
 * @param configProblems files that could not be read, with the reason
 */
public record EditorPayload(
        String catalog,
        Map<String, CustomMob> mobs,
        List<Macro> macros,
        Map<String, ItemStack> items,
        Map<String, List<GraphProblem>> problems,
        Map<String, List<GraphProblem>> macroProblems,
        Map<String, String> configProblems) {
    public static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(JacksonPaper.builder().build())
            .addModule(new EditorSerialization())
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .build();

    /**
     * @param configuration the loaded configuration
     * @param behaviours    the last compilation
     * @param catalog       version of the catalog the server runs
     * @return the payload
     */
    public static EditorPayload of(Configuration configuration, Behaviours behaviours, String catalog) {
        Map<String, CustomMob> mobs = new TreeMap<>();
        configuration.mobs().all().forEach(mob -> mobs.put(mob.id(), mob));
        return new EditorPayload(
                catalog,
                mobs,
                configuration.macros().macros(),
                new TreeMap<>(configuration.items().registrations()),
                behaviours.problems(),
                behaviours.macroProblems(),
                configuration.problems());
    }

    /**
     * @return the payload as JSON
     */
    public String asJson() {
        return MAPPER.writeValueAsString(this);
    }
}
