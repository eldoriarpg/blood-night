/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import de.eldoria.bloodnight.configuration.elements.world.AtmosphereSettings;
import de.eldoria.bloodnight.configuration.elements.world.NightRuleSettings;

import java.util.EnumSet;
import java.util.Set;

/**
 * A group of the plugin's data pack timelines that a world switches on and off as a whole. Each group has its own
 * world clock and timeline, both named {@code bloodnight:<name>}, so a group stays at rest wherever it is off.
 * <p>
 * Night groups start at clock time 0 when a blood night begins, are in full effect from {@value #FULL}, and fade out
 * from {@value #FADE_OUT} on, which the clock is set to when the night ends; dawn groups start at 0 when the night
 * ends. From {@link #rest()} on, a group changes nothing. Every track rests on a value its modifier leaves the world
 * alone with (alpha 0, times 1, {@code or false}, {@code and true}, an empty list), never on an {@code override}.
 */
public enum TimelineGroup {
    SKY("sky", false, Ticks.FADE_OUT + Ticks.FADE),
    RED_DAWN("red_dawn", true, Ticks.DAWN),
    THICK_FOG("thick_fog", false, Ticks.FADE_OUT + Ticks.FADE),
    DARKNESS("darkness", false, Ticks.FADE_OUT + Ticks.FADE),
    WORLD_PARTICLES("world_particles", false, Ticks.FADE_OUT + Ticks.FADE),
    MONSTERS_DONT_BURN("monsters_dont_burn", false, Ticks.FADE_OUT + Ticks.DAWN),
    CREAKINGS_WAKE("creakings_wake", false, Ticks.FADE_OUT + Ticks.FADE),
    RAIDS_ALLOWED("raids_allowed", false, Ticks.FADE_OUT + Ticks.FADE),
    RAIDS_DENIED("raids_denied", false, Ticks.FADE_OUT + Ticks.FADE);

    static final int FULL = Ticks.FADE;
    static final int FADE_OUT = Ticks.FADE_OUT;

    private final String name;
    private final boolean dawn;
    private final int rest;

    TimelineGroup(String name, boolean dawn, int rest) {
        this.name = name;
        this.dawn = dawn;
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
     * @return whether the group starts when the night ends instead of when it begins
     */
    public boolean dawn() {
        return dawn;
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

        private Ticks() {}
    }
}
