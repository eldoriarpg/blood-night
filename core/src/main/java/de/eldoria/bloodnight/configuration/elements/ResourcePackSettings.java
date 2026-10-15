/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements;

import dev.chojo.ocular.override.Env;
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.Prop;

/**
 * The BloodNight resource pack offered to players when they join: the red screen effect, red rain and red mist of
 * blood nights, and the names of mobs, enchantments and advancements in the player's language. Players who decline
 * still get everything else. The system property {@code bloodnight.resourcepack.url} or the environment variable
 * {@code BLOODNIGHT_RESOURCEPACK_URL} replace the configured URL, e.g. for a local test server.
 */
public class ResourcePackSettings {
    private boolean offer = true;
    private boolean mergeIntoPackPlugins = true;
    private boolean translateMobNames = true;

    @Overwrite(
            prop = @Prop("bloodnight.resourcepack.url"),
            env = @Env("BLOODNIGHT_RESOURCEPACK_URL"),
            description = "Where the resource pack is downloaded from")
    private String url = "https://bloodnight.docs.eldoria.de/pack/bloodnight.zip";

    private boolean required = false;
    private String prompt = "<red>BloodNight would like to show its blood nights with a few extra effects.";

    /**
     * @return whether the pack goes into installed pack plugins (Nexo, Oraxen, ItemsAdder), which then send it with
     *     their own pack instead of BloodNight offering it on its own
     */
    public boolean mergeIntoPackPlugins() {
        return mergeIntoPackPlugins;
    }

    /**
     * @return whether a mob's name is shown in each player's language, which name tags need the pack for; otherwise
     *     every player sees the name in the server's fallback language, with or without the pack
     */
    public boolean translateMobNames() {
        return translateMobNames;
    }

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
