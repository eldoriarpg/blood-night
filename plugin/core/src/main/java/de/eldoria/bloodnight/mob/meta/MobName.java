/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A mob's name in every language it has one in, and the style all of them share.
 * <p>
 * Players see the name in their own language: it is shown as the translatable text {@code bloodnight.mob.<key>},
 * which the server fills in for chat and boss bars (see {@link #texts()}) and the resource pack for name tags. Where
 * neither has the player's language, the text in the server's fallback language is shown, else English, else any.
 *
 * @param style MiniMessage opening tags put before the name in every language, e.g. {@code <dark_red>}; may be empty
 * @param texts plain text by language code, e.g. {@code en} or {@code pt_br}; empty shows no name
 */
public record MobName(String style, Map<String, String> texts) {
    /** The language whose text is shown where the fallback language has none. */
    public static final String ENGLISH = "en";

    /** A mob without a name. */
    public static final MobName NONE = new MobName("", Map.of());

    public MobName {
        style = Objects.requireNonNullElse(style, "").strip();
        Map<String, String> kept = new LinkedHashMap<>();
        Objects.requireNonNullElse(texts, Map.<String, String>of()).forEach((language, text) -> {
            if (language != null && text != null && !language.isBlank() && !text.isBlank()) {
                kept.put(language.strip().toLowerCase(Locale.ROOT), text.strip());
            }
        });
        texts = Map.copyOf(kept);
    }

    /**
     * @param english the name in English
     * @return a name with only an English text and no style
     */
    public static MobName english(String english) {
        return new MobName("", Map.of(ENGLISH, english));
    }

    /**
     * @return whether the mob shows no name
     */
    public boolean isEmpty() {
        return texts.isEmpty();
    }

    /**
     * @param language the language to prefer, e.g. the server's fallback language {@code de}
     * @return the text in that language, else in English, else the first by language code; empty without any
     */
    public String text(String language) {
        String preferred = texts.get(language);
        if (preferred != null) return preferred;
        String english = texts.get(ENGLISH);
        if (english != null) return english;
        return texts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse("");
    }

    /**
     * @param key      the name's translation key below {@code bloodnight.mob.}, e.g. the mob's id
     * @param fallback the server's fallback language, e.g. {@code en}
     * @return the name as MiniMessage, shown in each player's language; empty for a mob without a name. The fallback
     *     text sits in a quoted argument, which MiniMessage takes as it is, so only quotes and backslashes are escaped
     */
    public String miniMessage(String key, String fallback) {
        return miniMessage(key, fallback, true);
    }

    /**
     * @param key        the name's translation key below {@code bloodnight.mob.}, e.g. the mob's id
     * @param fallback   the server's fallback language, e.g. {@code en}
     * @param translated whether players see the name in their own language; otherwise everyone sees the fallback
     *                   language's text, and no resource pack is needed for it
     * @return the name as MiniMessage; empty for a mob without a name
     */
    public String miniMessage(String key, String fallback, boolean translated) {
        if (isEmpty()) return "";
        if (!translated) return style + MiniMessage.miniMessage().escapeTags(text(fallback));
        String text = text(fallback).replace("\\", "\\\\").replace("'", "\\'");
        return style + "<lang_or:" + translationKey(key) + ":'" + text + "'>";
    }

    /**
     * @param key the name's key below {@code bloodnight.mob.}, e.g. a mob's id
     * @return the translation key, e.g. {@code bloodnight.mob.armored_zombie}
     */
    public static String translationKey(String key) {
        return "bloodnight.mob." + key;
    }

    /**
     * A world's name over a mob's: the world's texts win per language, its style replaces the mob's when it has one.
     *
     * @param base the mob's own name
     * @return the name the mob has in that world
     */
    public MobName over(MobName base) {
        Map<String, String> merged = new LinkedHashMap<>(base.texts());
        merged.putAll(texts);
        return new MobName(style.isEmpty() ? base.style() : style, merged);
    }
}
