/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import de.eldoria.bloodnight.web.api.Language;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static net.kyori.adventure.text.Component.translatable;

/**
 * The texts the server renders for each player on its own, through Adventure's global translator: the death message
 * of the blood curse, the night names on boss bars, and the names of mobs. Mob names live in a store of their own,
 * swapped as a whole whenever they are registered again, since a translation store refuses a key twice.
 * <p>
 * Chat texts are not here; they go through {@link Texts}. These are MiniMessage in
 * {@code translations/<language>.properties}, and a text missing in a language falls back to the configured fallback
 * language.
 */
public final class Messages {
    /**
     * Death message of the data pack's damage type {@code bloodnight:blood_curse}, which clients have no text for;
     * with {@code .player} when someone dealt it.
     */
    public static final String BLOOD_CURSE_DEATH = "death.attack.bloodnight.blood_curse";

    private static final List<String> LOCALES =
            Arrays.stream(Language.values()).map(Language::language).toList();

    private final MiniMessageTranslationStore store =
            MiniMessageTranslationStore.create(Key.key("bloodnight", "messages"));
    private @Nullable MiniMessageTranslationStore names;

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
     * Replaces the mob names the server renders with these.
     *
     * @param byLanguage MiniMessage texts by key, by language, e.g. {@code en}
     * @param fallback   the language for players whose language has no name, e.g. {@code en}
     */
    public synchronized void names(Map<String, Map<String, String>> byLanguage, String fallback) {
        MiniMessageTranslationStore fresh = MiniMessageTranslationStore.create(Key.key("bloodnight", "mob_names"));
        byLanguage.forEach((language, texts) -> fresh.registerAll(parse(language), texts));
        fresh.defaultLocale(Locale.of(parse(fallback).getLanguage()));
        if (names != null) GlobalTranslator.translator().removeSource(names);
        GlobalTranslator.translator().addSource(fresh);
        names = fresh;
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
    public synchronized void unregister() {
        GlobalTranslator.translator().removeSource(store);
        if (names != null) GlobalTranslator.translator().removeSource(names);
        names = null;
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
        String path = "translations/%s.properties".formatted(locale);
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
