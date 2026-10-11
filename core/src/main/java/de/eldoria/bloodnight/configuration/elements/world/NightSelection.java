/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightSelectionCheck;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightSelectionType;

import java.util.HashMap;
import java.util.Map;

public class NightSelection {
    Map<NightSelectionType, NightSelectionCheck> settings = new HashMap<>() {
        {
            for (NightSelectionType value : NightSelectionType.values()) {
                put(value, value.create());
            }
        }
    };
    private final NightSelectionType current = NightSelectionType.INTERVAL;
}
