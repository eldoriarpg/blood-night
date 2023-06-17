package de.eldoria.bloodnight.configuration.worlds.nightselection.type;

import de.eldoria.bloodnight.configuration.worlds.nightselection.NightSelectionCheck;

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
