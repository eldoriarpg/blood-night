/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api;

import java.util.Locale;

/**
 * The languages BloodNight ships texts in, in the plugin, its resource pack and the editor, named by their locale as
 * the message files are, e.g.
 * {@code messages_de_DE.properties}.
 */
@SuppressWarnings("java:S115")
public enum Language {
    en_US,
    de_DE,
    es_ES,
    fr_FR,
    ms_MY,
    tr_TR,
    zh_CN;

    /**
     * @return the language alone, e.g. {@code de}
     */
    public String language() {
        return name().substring(0, name().indexOf('_'));
    }

    /**
     * @return the locale as Minecraft names its language files, e.g. {@code de_de}
     */
    public String minecraft() {
        return name().toLowerCase(Locale.ROOT);
    }
}
