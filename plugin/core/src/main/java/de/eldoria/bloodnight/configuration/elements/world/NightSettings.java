/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.configuration.elements.world.night.NightLength;
import de.eldoria.bloodnight.configuration.elements.world.night.TimedNights;

import java.util.ArrayList;
import java.util.List;

/**
 * When a world's nights are and how its blood nights behave.
 * <p>
 * {@code begin} and {@code end} are times of day in ticks (0 is sunrise); a night may wrap past midnight, e.g. 18000
 * to 2000. Commands run from the console when a blood night begins and ends; {@code {world}} is replaced by the
 * world's name, and a command with {@code {player}} runs once for every player in the world.
 * <p>
 * {@code blockedCommands} cannot be used by players during a blood night, e.g. {@code home} or {@code gamemode
 * creative}; aliases and namespaced forms of a command are blocked too.
 */
public class NightSettings {
    private int begin = 13_000;
    private int end = 23_000;
    private boolean skippable;
    private NightLength length = new NightLength();
    private List<String> beginCommands = new ArrayList<>();
    private List<String> endCommands = new ArrayList<>();
    private List<String> blockedCommands = new ArrayList<>();
    private TimedNights timed = new TimedNights();

    /**
     * @return the time of day nights begin, 0 to 23999
     */
    public int begin() {
        return Math.floorMod(begin, 24_000);
    }

    /**
     * @return the time of day nights end, 0 to 23999
     */
    public int end() {
        return Math.floorMod(end, 24_000);
    }

    /**
     * @return whether a blood night may be slept through
     */
    public boolean skippable() {
        return skippable;
    }

    /**
     * @return how long blood nights last
     */
    public NightLength length() {
        return length;
    }

    /**
     * @return console commands run when a blood night begins
     */
    public List<String> beginCommands() {
        return beginCommands;
    }

    /**
     * @return console commands run when a blood night ends
     */
    public List<String> endCommands() {
        return endCommands;
    }

    public List<String> blockedCommands() {
        return blockedCommands;
    }

    /**
     * @return when blood nights come in a world without a day cycle; such worlds use nothing else of these settings
     *     but the commands
     */
    public TimedNights timed() {
        return timed;
    }
}
