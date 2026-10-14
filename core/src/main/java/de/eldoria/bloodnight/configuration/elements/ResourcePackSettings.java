/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements;

/**
 * The BloodNight resource pack offered to players when they join: the red screen effect of blood nights. Players who
 * decline still get everything else.
 */
public class ResourcePackSettings {
    private boolean offer = true;
    private String url = "https://bloodnight.docs.eldoria.de/pack/bloodnight.zip";
    private boolean required = false;
    private String prompt = "<red>BloodNight would like to show its blood nights with a few extra effects.";

    /**
     * @return whether players are offered the pack when they join
     */
    public boolean offer() {
        return offer;
    }

    /**
     * @return where the pack is downloaded from
     */
    public String url() {
        return url;
    }

    /**
     * @return whether players who decline the pack are disconnected
     */
    public boolean required() {
        return required;
    }

    /**
     * @return the MiniMessage text shown with the offer
     */
    public String prompt() {
        return prompt;
    }
}
