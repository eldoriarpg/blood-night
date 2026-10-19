/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.night;

import de.eldoria.bloodnight.configuration.world.nightselection.Night;
import de.eldoria.bloodnight.configuration.world.nightselection.NightProbability;

/**
 * Blood nights for worlds without a day cycle, such as the Nether and the End: every {@code everyMinutes} a new
 * period starts, and with {@code chance} percent it begins with a blood night of {@code lengthMinutes}. Minutes are
 * game minutes of 1200 ticks, so they follow the server's tick rate.
 * <p>
 * In a world with a dragon fight, a dragon appearing also begins a blood night that lasts until the dragon dies, and
 * the dragon becomes the custom mob {@code dragon}, unless it is empty.
 */
public class TimedNights implements NightProbability {
    private int everyMinutes = 60;
    private int chance = 30;
    private int lengthMinutes = 10;
    private boolean dragonFight = true;
    private String dragon = "blood_dragon";

    /**
     * @return minutes from one period to the next, at least 1
     */
    public int everyMinutes() {
        return Math.max(everyMinutes, 1);
    }

    /**
     * @return how long a blood night lasts, at least 1 minute and less than a period
     */
    public int lengthMinutes() {
        return Math.clamp(lengthMinutes, 1, Math.max(everyMinutes() - 1, 1));
    }

    @Override
    public int probability(Night night) {
        return NightProbability.percent(chance);
    }

    /**
     * @return whether a dragon fight is a blood night
     */
    public boolean dragonFight() {
        return dragonFight;
    }

    /**
     * @return the id of the custom mob the dragon of such a fight becomes; empty keeps the vanilla dragon
     */
    public String dragon() {
        return dragon;
    }
}
