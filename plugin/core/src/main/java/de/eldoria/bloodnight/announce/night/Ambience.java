/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce.night;

import de.eldoria.bloodnight.configuration.world.announcement.SoundEntry;
import de.eldoria.bloodnight.configuration.world.presentation.SoundSettings;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Plays sounds of a blood night: the begin and end sounds, and ambience to each player at random intervals.
 */
public final class Ambience {
    private final Map<UUID, Integer> countdown = new ConcurrentHashMap<>();

    /**
     * Plays every sound of a list to an audience.
     */
    public static void play(Audience audience, SoundSettings settings, List<SoundEntry> sounds) {
        sounds.forEach(entry -> audience.playSound(sound(settings, entry)));
    }

    /**
     * Counts a second down for the player and plays one random ambience sound once the interval is over.
     *
     * @param player   a player in a world with a running blood night
     * @param settings the sound settings of the player's world
     */
    public void tick(Player player, SoundSettings settings) {
        int left = countdown.getOrDefault(player.getUniqueId(), interval(settings)) - 1;
        if (left > 0) {
            countdown.put(player.getUniqueId(), left);
            return;
        }
        countdown.put(player.getUniqueId(), interval(settings));
        List<SoundEntry> sounds = settings.ambience();
        if (sounds.isEmpty()) return;
        player.playSound(sound(settings, sounds.get(ThreadLocalRandom.current().nextInt(sounds.size()))));
    }

    /**
     * Forgets a player's countdown, e.g. when the blood night around them ended.
     */
    public void forget(Player player) {
        countdown.remove(player.getUniqueId());
    }

    private static int interval(SoundSettings settings) {
        return ThreadLocalRandom.current().nextInt(settings.minInterval(), settings.maxInterval() + 1);
    }

    private static Sound sound(SoundSettings settings, SoundEntry entry) {
        return Sound.sound(Key.key(entry.sound()), settings.channel(), entry.volume(), entry.pitch());
    }
}
