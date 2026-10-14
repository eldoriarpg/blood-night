/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static net.kyori.adventure.text.Component.translatable;

/**
 * The plugin's texts, translated for each player's client language.
 * <p>
 * Texts are MiniMessage in {@code messages/<language>.properties}, registered with Adventure's global translator, so
 * every message is a translatable component that the server renders in the receiver's language. A text missing in a
 * language falls back to the configured fallback language.
 */
public final class Messages {
    public static final String NIGHT_SLEEP = "bloodnight.night.sleep";
    public static final String COMMAND_BLOCKED = "bloodnight.command.blocked";
    private static final List<String> LOCALES = List.of("en", "de", "es", "ms", "tr", "zh");

    private final MiniMessageTranslationStore store =
            MiniMessageTranslationStore.create(Key.key("bloodnight", "messages"));

    /**
     * Loads the shipped texts and adds them to the global translator.
     *
     * @param fallback the language for players whose language has no text, e.g. {@code en_US}
     */
    public void register(String fallback) {
        for (String locale : LOCALES) {
            store.registerAll(parse(locale), load(locale));
        }
        fallback(fallback);
        GlobalTranslator.translator().addSource(store);
    }

    /**
     * @param fallback the language for players whose language has no text, e.g. {@code en_US}
     */
    public void fallback(String fallback) {
        store.defaultLocale(Locale.of(parse(fallback).getLanguage()));
    }

    /**
     * Removes the texts from the global translator.
     */
    public void unregister() {
        GlobalTranslator.translator().removeSource(store);
    }

    /**
     * @param key       the text's key
     * @param arguments MiniMessage arguments, e.g. {@code Argument.string("world", name)}
     * @return the text, rendered for each receiver
     */
    public static TranslatableComponent message(String key, ComponentLike... arguments) {
        return translatable(key, arguments);
    }

    private static Locale parse(String locale) {
        return Objects.requireNonNullElse(Translator.parseLocale(locale), Locale.US);
    }

    private static Map<String, String> load(String locale) {
        String path = "messages/%s.properties".formatted(locale);
        try (InputStream stream = Messages.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("Missing " + path);
            Properties properties = new Properties();
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            Map<String, String> texts = new HashMap<>();
            properties.forEach((key, value) -> texts.put((String) key, (String) value));
            return texts;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
