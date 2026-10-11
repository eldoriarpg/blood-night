/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.announcement;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A sound by its key, played with one of its pitches and volumes, picked at random each time.
 */
public class SoundEntry {
    private String sound = "minecraft:entity.lightning_bolt.thunder";
    private List<Float> pitches = List.of(1.0f);
    private List<Float> volumes = List.of(1.0f);

    SoundEntry() {}

    SoundEntry(String sound, List<Float> pitches, List<Float> volumes) {
        this.sound = sound;
        this.pitches = pitches;
        this.volumes = volumes;
    }

    public String sound() {
        return sound;
    }

    /**
     * @return one of the pitches, within what the client plays (0.5 to 2)
     */
    public float pitch() {
        return Math.clamp(pick(pitches), 0.5f, 2f);
    }

    /**
     * @return one of the volumes, at least 0
     */
    public float volume() {
        return Math.max(pick(volumes), 0f);
    }

    private static float pick(List<Float> values) {
        return values.isEmpty() ? 1f : values.get(ThreadLocalRandom.current().nextInt(values.size()));
    }
}
