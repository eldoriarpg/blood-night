package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.configuration.elements.world.mobsettings.MobSpawning;
import de.eldoria.bloodnight.mobs.MobSpawner;

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
