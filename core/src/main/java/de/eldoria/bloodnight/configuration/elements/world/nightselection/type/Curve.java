package de.eldoria.bloodnight.configuration.elements.world.nightselection.type;

import de.eldoria.bloodnight.configuration.elements.world.nightselection.NightSelectionCheck;

public class Curve implements NightSelectionCheck {
    /**
     * The min probability
     */
    int min = 20;
    /**
     * The max probability
     */
    int max = 80;
    /**
     * The amount of days until the curve reached its min point again
     */
    int period;
}
