/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.atmosphere;

import java.util.EnumSet;
import java.util.Set;

/**
 * Sudden frights during a blood night, each player on their own: a flinch as if hit from behind, a distant scream, the
 * elder guardian's face. Off unless an admin switches them on.
 */
public class Scares {
    private boolean enabled = false;
    private int chancePerMinute = 10;
    private Set<Scare> kinds = EnumSet.allOf(Scare.class);

    public boolean enabled() {
        return enabled;
    }

    /**
     * @return the chance in percent that a player gets a scare in a minute, 0 to 100
     */
    public int chancePerMinute() {
        return Math.clamp(chancePerMinute, 0, 100);
    }

    public Set<Scare> kinds() {
        return kinds;
    }
}
