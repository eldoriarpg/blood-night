package de.eldoria.bloodnight.configuration.worlds.nightselection.type;

import de.eldoria.bloodnight.configuration.worlds.nightselection.NightSelectionCheck;

import java.util.HashMap;
import java.util.Map;

public class MoonPhase  implements NightSelectionCheck {
    Map<MoonPhaseType, Integer> probability = new HashMap<>() {{
        put(MoonPhaseType.FULL_MOON, 0);
        put(MoonPhaseType.WANING_GIBBOUS, 10);
        put(MoonPhaseType.LAST_QUARTER, 20);
        put(MoonPhaseType.WANING_CRESCENT, 40);
        put(MoonPhaseType.NEW_MOON, 100);
        put(MoonPhaseType.WAXING_CRESCENT, 40);
        put(MoonPhaseType.FIRST_QUARTER, 20);
        put(MoonPhaseType.WAXING_GIBBOUS, 10);
    }};
}
