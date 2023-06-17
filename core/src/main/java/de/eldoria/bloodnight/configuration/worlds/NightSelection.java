package de.eldoria.bloodnight.configuration.worlds;

import de.eldoria.bloodnight.configuration.worlds.nightselection.NightSelectionCheck;
import de.eldoria.bloodnight.configuration.worlds.nightselection.NightSelectionType;

import java.util.HashMap;
import java.util.Map;

public class NightSelection {
    private NightSelectionType current = NightSelectionType.INTERVAL;

    Map<NightSelectionType, NightSelectionCheck> settings = new HashMap<>() {{
        for (NightSelectionType value : NightSelectionType.values()) {
            put(value, value.create());
        }
    }};
}
