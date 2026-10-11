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
    private static final List<Float> AMBIENCE_PITCHES = List.of(0.4f, 0.6f, 0.8f, 1.0f, 1.2f, 1.4f, 1.6f);
    private static final List<Float> AMBIENCE_VOLUMES = List.of(0.2f, 0.4f, 0.6f, 0.8f, 1.0f);

    private SoundEntries() {}

    public static SoundEntry thunder() {
        return new SoundEntry("minecraft:entity.lightning_bolt.thunder", List.of(0.8f, 1.0f), List.of(1.0f));
    }

    public static List<SoundEntry> ambience() {
        return List.of(
                        "minecraft:ambient.basalt_deltas.additions",
                        "minecraft:ambient.crimson_forest.additions",
                        "minecraft:ambient.nether_wastes.additions",
                        "minecraft:ambient.soul_sand_valley.additions")
                .stream()
                .map(sound -> new SoundEntry(sound, AMBIENCE_PITCHES, AMBIENCE_VOLUMES))
                .toList();
    }
}
