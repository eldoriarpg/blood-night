/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.legacy.LegacyConverter;
import de.eldoria.bloodnight.mob.CustomMob;
import org.bukkit.World;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The old BloodNight's files converted onto the server's current state, as the setup wizard offers to take them over.
 * Taking them over writes them into the session like any other edit; registered items are put back from a fresh
 * conversion when the session is applied, since their item stacks do not travel through the editor.
 *
 * @param main   {@code config.yml} as it would be
 * @param worlds the settings of each loaded world the old files name, by the world's UID
 * @param mobs   the mobs as they would be, by id
 * @param items  the items as they would be, by id
 * @param notes  what could not be taken over
 */
public record EditorLegacy(
        ConfigFile main,
        Map<String, SettingsPayload.WorldPayload> worlds,
        Map<String, CustomMob> mobs,
        Map<String, EditorItem> items,
        List<String> notes) {
    /**
     * @param result      the conversion
     * @param loadedWorlds the server's worlds
     * @return what the editor gets of it
     */
    public static EditorLegacy of(LegacyConverter.Result result, Collection<? extends World> loadedWorlds) {
        Map<String, SettingsPayload.WorldPayload> worlds = new TreeMap<>();
        for (World world : loadedWorlds) {
            var settings = result.worlds().get(world.getName());
            if (settings != null) {
                worlds.put(world.getUID().toString(), new SettingsPayload.WorldPayload(world.getName(), settings));
            }
        }
        Map<String, CustomMob> mobs = new TreeMap<>();
        result.mobs().forEach(mob -> mobs.put(mob.id(), mob));
        return new EditorLegacy(result.main(), worlds, mobs, EditorPayload.items(result.items()), result.notes());
    }
}
