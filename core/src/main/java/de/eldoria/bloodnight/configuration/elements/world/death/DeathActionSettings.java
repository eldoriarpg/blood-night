/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.death;

/**
 * Death actions during blood nights: for players, and for every enemy that dies.
 */
public class DeathActionSettings {
    private PlayerDeathActions player = new PlayerDeathActions();
    private DeathActions mob = new DeathActions();

    public PlayerDeathActions player() {
        return player;
    }

    public DeathActions mob() {
        return mob;
    }
}
