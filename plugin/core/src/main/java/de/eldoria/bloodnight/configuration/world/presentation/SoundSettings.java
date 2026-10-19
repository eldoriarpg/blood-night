/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.presentation;

import de.eldoria.bloodnight.configuration.world.announcement.SoundEntries;
import de.eldoria.bloodnight.configuration.world.announcement.SoundEntry;
import net.kyori.adventure.sound.Sound;

import java.util.ArrayList;
import java.util.List;

/**
 * Sounds of a blood night: played to the players in the world when it begins and ends, and one ambience sound every
 * {@code minInterval} to {@code maxInterval} seconds in between, on {@code channel}.
 */
public class SoundSettings {
    private Sound.Source channel = Sound.Source.AMBIENT;
    private int minInterval = 10;
    private int maxInterval = 40;
    private List<SoundEntry> begin = new ArrayList<>(List.of(SoundEntries.thunder()));
    private List<SoundEntry> end = new ArrayList<>(List.of(SoundEntries.thunder()));
    private List<SoundEntry> ambience = new ArrayList<>(SoundEntries.ambience());

    public Sound.Source channel() {
        return channel;
    }

    /**
     * @return the shortest pause between ambience sounds in seconds, at least 1
     */
    public int minInterval() {
        return Math.max(minInterval, 1);
    }

    /**
     * @return the longest pause between ambience sounds in seconds, at least the shortest
     */
    public int maxInterval() {
        return Math.max(maxInterval, minInterval());
    }

    public List<SoundEntry> begin() {
        return begin;
    }

    public List<SoundEntry> end() {
        return end;
    }

    public List<SoundEntry> ambience() {
        return ambience;
    }
}
