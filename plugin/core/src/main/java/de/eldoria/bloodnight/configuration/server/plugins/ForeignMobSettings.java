/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.server.plugins;

import java.util.ArrayList;
import java.util.List;

/**
 * How BloodNight treats mobs other plugins own: Citizens NPCs, MythicMobs mobs, and entities carrying one of the
 * {@code markerKeys} in their data, such as {@code levelledmobs:level}.
 */
public class ForeignMobSettings {
    private boolean wrap = false;
    private boolean rules = false;
    private List<String> markerKeys = new ArrayList<>();

    /**
     * @return whether such mobs may become BloodNight mobs
     */
    public boolean wrap() {
        return wrap;
    }

    /**
     * @return whether blood night rules (damage, health, death actions, loot) apply to such mobs
     */
    public boolean rules() {
        return rules;
    }

    /**
     * @return data keys, as {@code namespace:key}, that mark an entity as another plugin's
     */
    public List<String> markerKeys() {
        return markerKeys;
    }
}
