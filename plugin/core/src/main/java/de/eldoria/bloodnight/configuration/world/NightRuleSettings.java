/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world;

import de.eldoria.bloodnight.configuration.world.rules.DifficultyChange;
import de.eldoria.bloodnight.configuration.world.rules.RaidChange;
import de.eldoria.bloodnight.configuration.world.rules.WeatherChange;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * How the world itself changes while a blood night runs. Everything is given back when the night ends, unless someone
 * changed it in the meantime.
 * <p>
 * {@code monstersDontBurn}, {@code creakingsWake} and {@code raids} work through the plugin's data pack and end with
 * the night; monsters stay unburnt through the following dawn. By default only creakings wake.
 */
public class NightRuleSettings {
    private DifficultyChange difficulty = DifficultyChange.UNCHANGED;
    private int monsterSpawnLimit = -1;
    private int monsterTicksPerSpawn = -1;
    private WeatherChange weather = WeatherChange.UNCHANGED;
    private Map<String, String> gameRules = new LinkedHashMap<>();
    private boolean phantomsOnlyAtBloodNights = false;
    private boolean monstersDontBurn = false;
    private boolean creakingsWake = true;
    private RaidChange raids = RaidChange.UNCHANGED;

    /**
     * @return the world's difficulty during the night
     */
    public DifficultyChange difficulty() {
        return difficulty;
    }

    /**
     * @return how many monsters may be around each player during the night; below 0 leaves it unchanged
     */
    public int monsterSpawnLimit() {
        return monsterSpawnLimit;
    }

    /**
     * @return every how many ticks monsters try to spawn during the night; below 0 leaves it unchanged
     */
    public int monsterTicksPerSpawn() {
        return monsterTicksPerSpawn;
    }

    /**
     * @return the weather during the night
     */
    public WeatherChange weather() {
        return weather;
    }

    /**
     * @return game rules set during the night, by key such as {@code minecraft:spawn_phantoms}
     */
    public Map<String, String> gameRules() {
        return gameRules;
    }

    /**
     * @return whether phantoms only spawn on their own during blood nights
     */
    public boolean phantomsOnlyAtBloodNights() {
        return phantomsOnlyAtBloodNights;
    }

    /**
     * @return whether monsters survive the sunlight during the night and the dawn after it
     */
    public boolean monstersDontBurn() {
        return monstersDontBurn;
    }

    /**
     * @return whether creakings hunt during the night
     */
    public boolean creakingsWake() {
        return creakingsWake;
    }

    /**
     * @return whether raids may start during the night
     */
    public RaidChange raids() {
        return raids;
    }
}
