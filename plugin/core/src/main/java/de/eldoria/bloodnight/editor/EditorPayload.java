/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.loot.LootTable;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.serialization.EditorSerialization;
import de.eldoria.bloodnight.store.MobStore;
import de.eldoria.graph.compiler.validate.GraphProblem;
import de.eldoria.graph.model.Macro;
import de.eldoria.jacksonbukkit.JacksonPaper;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * What the editor gets from a server: the user data, plus the version of the catalog it was made for.
 * <p>
 * The catalog itself (nodes, ports, types) is generated at build time and loaded by the editor on its own; the
 * payload only names it, so the editor can tell when a server runs a different platform.
 *
 * @param catalog        version of the catalog the server runs
 * @param mobs           mob definitions by id, including their behaviour graphs
 * @param macros         the macro library
 * @param items          the items by id: registered from the game (name, lore and enchantments editable) and made in
 *                       the editor
 * @param lootTables     the loot tables by id
 * @param problems       problems per mob id
 * @param macroProblems  problems per macro id
 * @param configProblems files that could not be read, with the reason
 * @param settings       the server-wide settings and every world's settings
 * @param choices        the materials and enchantments the server offers
 * @param hashes         SHA-256 of each file the session may replace, as it was when the session opened; checked
 *                       when the result comes back
 * @param store          the mobs the store offers and how the installed ones stood to it, read only
 * @param setup          whether the session was opened by {@code /bloodnight setup}, so the editor starts with the
 *                       setup wizard
 * @param legacy         the old BloodNight's files converted, for the setup wizard to offer; read only
 */
public record EditorPayload(
        String catalog,
        Map<String, CustomMob> mobs,
        List<Macro> macros,
        Map<String, EditorItem> items,
        Map<String, LootTable> lootTables,
        Map<String, List<GraphProblem>> problems,
        Map<String, List<GraphProblem>> macroProblems,
        Map<String, String> configProblems,
        SettingsPayload settings,
        EditorChoices choices,
        Map<String, String> hashes,
        EditorStore store,
        boolean setup,
        @Nullable EditorLegacy legacy) {
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
     * @param store         the mob store
     * @param aliases       a world's name in a world plugin, empty for none
     * @return the payload
     * @throws IOException when a file cannot be hashed
     */
    public static EditorPayload of(
            Configuration configuration,
            Behaviours behaviours,
            String catalog,
            Collection<? extends World> worlds,
            MobStore store,
            Function<World, String> aliases)
            throws IOException {
        SettingsPayload settings = settings(configuration, worlds, aliases);
        Map<String, CustomMob> mobs = new TreeMap<>();
        configuration.mobs().all().forEach(mob -> mobs.put(mob.id(), mob));
        return new EditorPayload(
                catalog,
                mobs,
                configuration.macros().macros(),
                items(configuration.items()),
                configuration.lootTables().tables(),
                behaviours.problems(),
                behaviours.macroProblems(),
                configuration.problems(),
                settings,
                EditorChoices.ofServer(),
                configuration.fileHashes(worlds),
                EditorStore.of(store, configuration.mobs().all()),
                false,
                null);
    }

    /**
     * @param legacy the old BloodNight's files converted, if the server has them
     * @return this payload for a session the editor opens with the setup wizard
     */
    public EditorPayload forSetup(@Nullable EditorLegacy legacy) {
        return new EditorPayload(
                catalog,
                mobs,
                macros,
                items,
                lootTables,
                problems,
                macroProblems,
                configProblems,
                settings,
                choices,
                hashes,
                store,
                true,
                legacy);
    }

    static Map<String, EditorItem> items(ItemRegistry registry) {
        Map<String, EditorItem> items = new TreeMap<>();
        registry.registrations().forEach((id, item) -> items.put(id, EditorItem.registered(item)));
        registry.definitions().forEach((id, definition) -> items.put(id, EditorItem.defined(definition)));
        return items;
    }

    private static SettingsPayload settings(
            Configuration configuration, Collection<? extends World> worlds, Function<World, String> aliases) {
        Map<String, SettingsPayload.WorldPayload> settings = new TreeMap<>();
        for (World world : worlds) {
            settings.put(
                    world.getUID().toString(),
                    SettingsPayload.WorldPayload.of(world, configuration.worldConfig(world), aliases.apply(world)));
        }
        return new SettingsPayload(configuration.main(), settings);
    }

    /**
     * @return the payload as JSON
     */
    public String asJson() {
        return MAPPER.writeValueAsString(this);
    }
}
