package de.eldoria.bloodnight.configuration.elements.world.nightselection;

import de.eldoria.bloodnight.configuration.elements.world.nightselection.type.Interval;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.type.MoonPhase;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.type.Curve;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.type.Phase;
import de.eldoria.bloodnight.configuration.elements.world.nightselection.type.Random;

import java.util.function.Supplier;

public enum NightSelectionType {
    /**
     * Simply choose a random entityType.
     */
    RANDOM(Random::new),
    /**
     * Based on the current ingame moon phase.
     */
    MOON_PHASE(MoonPhase::new),
    /**
     * Based on the current real world moon phase.
     */
    REAL_MOON_PHASE(MoonPhase::new),
    /**
     * Based on an interval and probability.
     */
    INTERVAL(Interval::new),
    /**
     * Based on daily different probability.
     */
    PHASE(Phase::new),
    /**
     * Based on a curve defining the probability.
     */
    CURVE(Curve::new);

    private final Supplier<NightSelectionCheck> create;

    NightSelectionType(Supplier<NightSelectionCheck> create) {
        this.create = create;
    }

    public NightSelectionCheck create() {
        return create.get();
    }
}
