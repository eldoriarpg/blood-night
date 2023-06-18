package de.eldoria.bloodnight.configuration.elements.world.mobsettings;

import java.util.Set;

public class MobSpawning {
    /**
     * The percentage of wrapped mobs
     */
    int spawnPercentage = 50;
    /**
     * The mobs which are permitted to spawn.
     */
    private Set<String> activeTypes;
}
