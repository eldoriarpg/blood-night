/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.nightselection.type;

import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightSelectionCheck;

public class Interval implements NightSelectionCheck {
    /**
     * Every n days can be a blood night.
     */
    int interval = 6;
    /**
     * Probability of the night becoming a blood night.
     */
    int probability;
}
