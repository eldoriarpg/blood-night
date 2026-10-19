/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.world.presentation;

import de.eldoria.bloodnight.configuration.world.atmosphere.AmbientParticle;
import de.eldoria.bloodnight.configuration.world.atmosphere.Scares;

import java.util.ArrayList;
import java.util.List;

/**
 * How a blood night looks and feels to the players in the world, on top of the announcement.
 * <p>
 * {@code sky} turns the sky, fog, clouds and light red through the plugin's data pack, hangs the clouds low and dark
 * and hides the stars, so the night looks overcast without rain. Also through the data pack: {@code redDawn} colours the
 * sunrise after the night red, {@code thickFog} closes the fog in over the night, {@code darkness} darkens the night
 * and lets more monsters spawn, and {@code worldParticles} lets the client draw ash and spores in the world, in place
 * of {@code particles}. Before a blood night, {@code omen} turns the sunset deep red and lets the first ash fall,
 * {@code omenSounds} rings distant bells and howls, and {@code restlessAnimals} makes animals near players bolt. The
 * rest happens for each player on their own client
 * and ends with the night: a red screen edge, short darkness pulses, rain (off by default), particles around
 * the camera, a heartbeat, the resource pack's screen effect and, when switched on, scares.
 */
public class AtmosphereSettings {
    private boolean sky = true;
    private boolean redDawn = true;
    private boolean thickFog = false;
    private boolean darkness = false;
    private boolean worldParticles = true;
    private boolean omen = true;
    private boolean omenSounds = true;
    private boolean restlessAnimals = true;
    private boolean vignette = true;
    private boolean darknessPulses = true;
    private boolean rain = false;
    private boolean heartbeat = true;
    private double heartbeatRange = 30;
    private double heartbeatLoudestWithin = 5;
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
     * @return whether the sunrise after a blood night glows red
     */
    public boolean redDawn() {
        return redDawn;
    }

    /**
     * @return whether the fog closes in over the night
     */
    public boolean thickFog() {
        return thickFog;
    }

    /**
     * @return whether the night is darker, so more monsters spawn
     */
    public boolean darkness() {
        return darkness;
    }

    /**
     * @return whether the client draws ash and spores in the world; {@link #particles()} are then not sent
     */
    public boolean worldParticles() {
        return worldParticles;
    }

    /**
     * @return whether the sunset before a blood night glows deep red, with the first ash falling
     */
    public boolean omen() {
        return omen;
    }

    /**
     * @return whether players hear distant bells and howls before a blood night
     */
    public boolean omenSounds() {
        return omenSounds;
    }

    /**
     * @return whether animals near players bolt before a blood night
     */
    public boolean restlessAnimals() {
        return restlessAnimals;
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
     * @return how far away a monster can be for the heartbeat to start, in blocks, at least 1
     */
    public double heartbeatRange() {
        return Math.max(heartbeatRange, 1);
    }

    /**
     * @return within how many blocks of a monster the heartbeat is at its loudest and fastest, below the range
     */
    public double heartbeatLoudestWithin() {
        return Math.clamp(heartbeatLoudestWithin, 0, heartbeatRange() - 0.5);
    }

    /**
     * @return whether players with the resource pack see its red screen effect
     */
    public boolean screenEffect() {
        return screenEffect;
    }

    /**
     * @return the particles drifting around each player, unless {@link #worldParticles()} draws them
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
