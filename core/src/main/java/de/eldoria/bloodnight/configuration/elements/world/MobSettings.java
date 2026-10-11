/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.configuration.elements.world.mobsettings.MobSpawning;

public class MobSettings {
    private final MobSpawning spawning;

    public MobSettings(MobSpawning spawning) {
        this.spawning = spawning;
    }

    public MobSettings() {
        spawning = new MobSpawning();
    }

    public MobSpawning spawning() {
        return spawning;
    }
}
