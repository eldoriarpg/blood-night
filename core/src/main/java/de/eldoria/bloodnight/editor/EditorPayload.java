/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.serialization.EditorSerialization;
import de.eldoria.graph.compiler.GraphProblem;
import de.eldoria.graph.model.Macro;
import de.eldoria.jacksonbukkit.JacksonPaper;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Collection;
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
 * @param items          registered items by id; the editor assigns them, it cannot create or change them
 * @param problems       problems per mob id
 * @param macroProblems  problems per macro id
 * @param configProblems files that could not be read, with the reason
 * @param settings       the server-wide settings, the global drops and every world's settings
 * @param hashes         SHA-256 of each file the session may replace, as it was when the session opened; checked
 *                       when the result comes back
 */
public record EditorPayload(
        String catalog,
        Map<String, CustomMob> mobs,
        List<Macro> macros,
        Map<String, ItemStack> items,
        Map<String, List<GraphProblem>> problems,
        Map<String, List<GraphProblem>> macroProblems,
        Map<String, String> configProblems,
        SettingsPayload settings,
        Map<String, String> hashes) {
    public static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(JacksonPaper.builder().build())
            .addModule(new EditorSerialization())
            .changeDefaultVisibility(visibility -> visibility
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .build();

    /**
     * @param configuration the loaded configuration
     * @param behaviours    the last compilation
     * @param catalog       version of the catalog the server runs
     * @param worlds        the server's worlds
     * @return the payload
     * @throws IOException when a file cannot be hashed
     */
    public static EditorPayload of(
            Configuration configuration, Behaviours behaviours, String catalog, Collection<? extends World> worlds)
            throws IOException {
        SettingsPayload settings = settings(configuration, worlds);
        Map<String, CustomMob> mobs = new TreeMap<>();
        configuration.mobs().all().forEach(mob -> mobs.put(mob.id(), mob));
        return new EditorPayload(
                catalog,
                mobs,
                configuration.macros().macros(),
                new TreeMap<>(configuration.items().registrations()),
                behaviours.problems(),
                behaviours.macroProblems(),
                configuration.problems(),
                settings,
                configuration.fileHashes(worlds));
    }

    private static SettingsPayload settings(Configuration configuration, Collection<? extends World> worlds) {
        Map<String, SettingsPayload.WorldPayload> settings = new TreeMap<>();
        for (World world : worlds) {
            settings.put(
                    world.getUID().toString(),
                    new SettingsPayload.WorldPayload(world.getName(), configuration.worldConfig(world)));
        }
        return new SettingsPayload(configuration.main(), configuration.globalDrops(), settings);
    }

    /**
     * @return the payload as JSON
     */
    public String asJson() {
        return MAPPER.writeValueAsString(this);
    }
}
