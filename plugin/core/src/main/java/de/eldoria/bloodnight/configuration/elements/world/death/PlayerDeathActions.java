/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.death;

import java.util.ArrayList;
import java.util.List;

/**
 * What happens when a player dies during a blood night, on top of lightning and shockwave.
 * <p>
 * The player loses their dropped items and experience by chance in percent. Death and respawn commands run from the
 * console with {@code {player}} and {@code {world}} replaced; respawn effects are given on respawn in a world with a
 * running blood night.
 */
public class PlayerDeathActions extends DeathActions {
    private int loseInventoryChance;
    private int loseExperienceChance;
    private List<String> deathCommands = new ArrayList<>();
    private List<String> respawnCommands = new ArrayList<>();
    private List<TimedEffect> respawnEffects = new ArrayList<>(List.of(new TimedEffect("minecraft:nausea", 5)));

    /**
     * @return the chance the dropped items are destroyed, 0 to 100
     */
    public int loseInventoryChance() {
        return Math.clamp(loseInventoryChance, 0, 100);
    }

    /**
     * @return the chance the dropped experience is lost, 0 to 100
     */
    public int loseExperienceChance() {
        return Math.clamp(loseExperienceChance, 0, 100);
    }

    public List<String> deathCommands() {
        return deathCommands;
    }

    public List<String> respawnCommands() {
        return respawnCommands;
    }

    public List<TimedEffect> respawnEffects() {
        return respawnEffects;
    }
}
