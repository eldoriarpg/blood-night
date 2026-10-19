/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.announcement;

/**
 * A sound by its key, played with a pitch and a volume picked at random from their ranges each time.
 */
public class SoundEntry {
    private String sound = "minecraft:entity.lightning_bolt.thunder";
    private SoundRange pitch = new SoundRange();
    private SoundRange volume = new SoundRange();

    SoundEntry() {}

    SoundEntry(String sound, SoundRange pitch, SoundRange volume) {
        this.sound = sound;
        this.pitch = pitch;
        this.volume = volume;
    }

    public String sound() {
        return sound;
    }

    /**
     * @return a pitch from the range, within what the client plays (0.5 to 2)
     */
    public float pitch() {
        return pitch.pick(0.5f, 2f);
    }

    /**
     * @return a volume from the range, 0 to 1; more would carry further, not play louder
     */
    public float volume() {
        return volume.pick(0f, 1f);
    }
}
