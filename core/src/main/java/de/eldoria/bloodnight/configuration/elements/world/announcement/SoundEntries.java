/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.announcement;

import java.util.List;

/**
 * The alpha's default sounds: thunder when a blood night begins and ends, the nether's eerie additions as ambience.
 */
public final class SoundEntries {

    private SoundEntries() {}

    public static SoundEntry thunder() {
        return new SoundEntry(
                "minecraft:entity.lightning_bolt.thunder", new SoundRange(0.8f, 1.0f), new SoundRange(1.0f, 1.0f));
    }

    public static List<SoundEntry> ambience() {
        return List.of(
                        "minecraft:ambient.basalt_deltas.additions",
                        "minecraft:ambient.crimson_forest.additions",
                        "minecraft:ambient.nether_wastes.additions",
                        "minecraft:ambient.soul_sand_valley.additions")
                .stream()
                .map(sound -> new SoundEntry(sound, new SoundRange(0.5f, 1.6f), new SoundRange(0.2f, 1.0f)))
                .toList();
    }
}
