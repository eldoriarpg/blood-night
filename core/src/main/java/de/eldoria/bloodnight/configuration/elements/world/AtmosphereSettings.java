/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world;

import de.eldoria.bloodnight.configuration.elements.world.atmosphere.AmbientParticle;
import de.eldoria.bloodnight.configuration.elements.world.atmosphere.Scares;

import java.util.ArrayList;
import java.util.List;

/**
 * How a blood night looks and feels to the players in the world, on top of the announcement.
 * <p>
 * {@code sky} turns the sky, fog, clouds and light red through the plugin's data pack. The rest happens for each
 * player on their own client and ends with the night: a red screen edge, short darkness pulses, rain, particles around
 * the camera, a heartbeat, the resource pack's screen effect and, when switched on, scares.
 */
public class AtmosphereSettings {
    private boolean sky = true;
    private boolean vignette = true;
    private boolean darknessPulses = true;
    private boolean rain = true;
    private boolean heartbeat = true;
    private boolean screenEffect = true;
    private List<AmbientParticle> particles = new ArrayList<>(
            List.of(new AmbientParticle("minecraft:ash", 12), new AmbientParticle("minecraft:crimson_spore", 4)));
    private Scares scares = new Scares();

    /**
     * @return whether the sky, fog, clouds and light turn red
     */
    public boolean sky() {
        return sky;
    }

    /**
     * @return whether the edge of the screen glows red
     */
    public boolean vignette() {
        return vignette;
    }

    /**
     * @return whether the screen goes dark for a moment now and then
     */
    public boolean darknessPulses() {
        return darknessPulses;
    }

    /**
     * @return whether it rains for the players during the night
     */
    public boolean rain() {
        return rain;
    }

    /**
     * @return whether players hear a heartbeat while monsters are near
     */
    public boolean heartbeat() {
        return heartbeat;
    }

    /**
     * @return whether players with the resource pack see its red screen effect
     */
    public boolean screenEffect() {
        return screenEffect;
    }

    /**
     * @return the particles drifting around each player
     */
    public List<AmbientParticle> particles() {
        return particles;
    }

    /**
     * @return the sudden frights, off unless switched on
     */
    public Scares scares() {
        return scares;
    }
}
