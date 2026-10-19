/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere.timeline;

import de.eldoria.bloodnight.configuration.world.NightRuleSettings;
import de.eldoria.bloodnight.configuration.world.presentation.AtmosphereSettings;

import java.util.EnumSet;
import java.util.Set;

/**
 * A group of the plugin's data pack timelines that a world switches on and off as a whole. Each group has its own
 * world clock and timeline, both named {@code bloodnight:<name>}, so a group stays at rest wherever it is off.
 * <p>
 * Night groups start at clock time 0 when a blood night begins, are in full effect from {@value #FULL}, and fade out
 * from {@value #FADE_OUT} on, which the clock is set to when the night ends; dawn groups start at 0 when the night
 * ends; omen groups start {@value #OMEN_TICKS} ticks before a blood night begins and fade out as it begins. From
 * {@link #rest()} on, a group changes nothing. Every track rests on a value its modifier leaves the world
 * alone with (alpha 0, times 1, {@code or false}, {@code and true}, an empty list), never on an {@code override}.
 */
public enum TimelineGroup {
    SKY("sky", Start.NIGHT, Ticks.FADE_OUT + Ticks.FADE),
    RED_DAWN("red_dawn", Start.DAWN, Ticks.DAWN),
    THICK_FOG("thick_fog", Start.NIGHT, Ticks.FADE_OUT + Ticks.FADE),
    DARKNESS("darkness", Start.NIGHT, Ticks.FADE_OUT + Ticks.FADE),
    WORLD_PARTICLES("world_particles", Start.NIGHT, Ticks.FADE_OUT + Ticks.FADE),
    MONSTERS_DONT_BURN("monsters_dont_burn", Start.NIGHT, Ticks.FADE_OUT + Ticks.DAWN),
    CREAKINGS_WAKE("creakings_wake", Start.NIGHT, Ticks.FADE_OUT + Ticks.FADE),
    RAIDS_ALLOWED("raids_allowed", Start.NIGHT, Ticks.FADE_OUT + Ticks.FADE),
    RAIDS_DENIED("raids_denied", Start.NIGHT, Ticks.FADE_OUT + Ticks.FADE),
    OMEN("omen", Start.OMEN, Ticks.OMEN + Ticks.FADE);

    public static final int FULL = Ticks.FADE;
    public static final int FADE_OUT = Ticks.FADE_OUT;
    public static final int OMEN_TICKS = Ticks.OMEN;

    private final String name;
    private final Start start;
    private final int rest;

    TimelineGroup(String name, Start start, int rest) {
        this.name = name;
        this.start = start;
        this.rest = rest;
    }

    /**
     * @return the groups a world has switched on
     */
    public static Set<TimelineGroup> of(AtmosphereSettings atmosphere, NightRuleSettings rules) {
        Set<TimelineGroup> groups = EnumSet.noneOf(TimelineGroup.class);
        if (atmosphere.sky()) groups.add(SKY);
        if (atmosphere.redDawn()) groups.add(RED_DAWN);
        if (atmosphere.thickFog()) groups.add(THICK_FOG);
        if (atmosphere.darkness()) groups.add(DARKNESS);
        if (atmosphere.worldParticles()) groups.add(WORLD_PARTICLES);
        if (atmosphere.omen()) groups.add(OMEN);
        if (rules.monstersDontBurn()) groups.add(MONSTERS_DONT_BURN);
        if (rules.creakingsWake()) groups.add(CREAKINGS_WAKE);
        switch (rules.raids()) {
            case ALLOW -> groups.add(RAIDS_ALLOWED);
            case DENY -> groups.add(RAIDS_DENIED);
            case UNCHANGED -> {}
        }
        return groups;
    }

    /**
     * @return the key of the group's clock and timeline, e.g. {@code bloodnight:sky}
     */
    public String key() {
        return "bloodnight:" + name;
    }

    /**
     * @return when the group starts
     */
    public Start start() {
        return start;
    }

    /**
     * @return the clock time from which the group changes nothing
     */
    public int rest() {
        return rest;
    }

    /**
     * Lengths of the timelines' phases, in ticks.
     */
    static final class Ticks {
        static final int FADE = 200;
        static final int DAWN = 2400;
        static final int FADE_OUT = 1_000_000;
        static final int OMEN = 1500;

        private Ticks() {}
    }

    /**
     * When a group's clock starts at 0.
     */
    public enum Start {
        /** When a blood night begins. */
        NIGHT,
        /** When a blood night ends. */
        DAWN,
        /** {@value TimelineGroup#OMEN_TICKS} ticks before a blood night begins. */
        OMEN
    }
}
