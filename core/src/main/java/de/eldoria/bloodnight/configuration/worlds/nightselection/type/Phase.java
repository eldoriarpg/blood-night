package de.eldoria.bloodnight.configuration.worlds.nightselection.type;

import de.eldoria.bloodnight.configuration.worlds.nightselection.NightSelectionCheck;

import java.util.ArrayList;
import java.util.List;

public class Phase implements NightSelectionCheck {
    List<Integer> probabilities = new ArrayList<>() {{
        add(20);
        add(50);
        add(20);
        add(10);
    }};
}
