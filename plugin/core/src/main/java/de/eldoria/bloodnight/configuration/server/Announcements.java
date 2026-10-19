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
 * {@code prefix} is MiniMessage put before chat messages. That a blood night begins or ends is shown to the players
 * in its world as {@code method} says, and to everyone else as {@code elsewhereMethod} says; {@link Method#NONE} tells
 * nobody there. {@code personalMethod} is how messages to a single player are shown, such as the warning
 * when entering a world with a running blood night. {@code fallbackLocale} is the language for players whose client
 * language has no translation. {@code money} is whether players are told the money a kill paid them.
 */
public class Announcements {
    private String prefix = "<dark_red>[<red>BN</red>]</dark_red> ";
    private Method method = Method.TITLE;
    private Method elsewhereMethod = Method.CHAT;
    private Method personalMethod = Method.ACTION_BAR;
    private Language fallbackLocale = Language.en_US;
    private boolean money = true;

    public String prefix() {
        return prefix;
    }

    public Method method() {
        return method;
    }

    public Method elsewhereMethod() {
        return elsewhereMethod;
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
     * Where a message is shown; {@link #NONE} does not show it.
     */
    public enum Method {
        CHAT,
        TITLE,
        SUBTITLE,
        ACTION_BAR,
        NONE
    }
}
