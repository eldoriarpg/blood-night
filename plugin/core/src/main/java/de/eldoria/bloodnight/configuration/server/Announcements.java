/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.server;

import de.eldoria.bloodnight.web.api.Language;

/**
 * How blood nights are announced, server-wide.
 * <p>
 * {@code prefix} is MiniMessage put before chat messages. {@code reach} says who hears that a blood night begins or
 * ends, {@code method} how; {@code personalMethod} is how messages to a single player are shown, such as the warning
 * when entering a world with a running blood night. {@code fallbackLocale} is the language for players whose client
 * language has no translation. {@code money} is whether players are told the money a kill paid them.
 */
public class Announcements {
    private String prefix = "<dark_red>[<red>BN</red>]</dark_red> ";
    private Reach reach = Reach.SERVER;
    private Method method = Method.SUBTITLE;
    private Method personalMethod = Method.ACTION_BAR;
    private Language fallbackLocale = Language.en_US;
    private boolean money = true;

    public String prefix() {
        return prefix;
    }

    public Reach reach() {
        return reach;
    }

    public Method method() {
        return method;
    }

    public Method personalMethod() {
        return personalMethod;
    }

    public Language fallbackLocale() {
        return fallbackLocale;
    }

    public boolean money() {
        return money;
    }

    /**
     * Who hears that a blood night begins or ends.
     */
    public enum Reach {
        /**
         * Everyone online.
         */
        SERVER,
        /**
         * The players in the world.
         */
        WORLD,
        /**
         * Nobody.
         */
        NONE
    }

    /**
     * Where a message is shown.
     */
    public enum Method {
        CHAT,
        TITLE,
        SUBTITLE,
        ACTION_BAR
    }
}
