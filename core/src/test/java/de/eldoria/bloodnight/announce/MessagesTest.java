/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessagesTest {
    private final Messages messages = new Messages();

    @BeforeEach
    void setUp() {
        messages.register("en_US");
    }

    @AfterEach
    void tearDown() {
        messages.unregister();
    }

    private static String render(Component component, Locale locale) {
        return PlainTextComponentSerializer.plainText().serialize(GlobalTranslator.render(component, locale));
    }

    private static String begin(Locale locale) {
        return render(Messages.message(Messages.NIGHT_BEGIN, Argument.string("world", "world")), locale);
    }

    private static Properties bundle(String language) throws IOException {
        try (InputStream stream =
                MessagesTest.class.getClassLoader().getResourceAsStream("messages/%s.properties".formatted(language))) {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return properties;
        }
    }

    @Test
    void englishHasEveryText() throws IOException {
        assertEquals(
                Set.of(
                        Messages.NIGHT_BEGIN,
                        Messages.NIGHT_END,
                        Messages.NIGHT_JOINED,
                        Messages.NIGHT_LEFT,
                        Messages.NIGHT_SLEEP,
                        Messages.COMMAND_BLOCKED,
                        "bloodnight.bossbar.title"),
                bundle("en").stringPropertyNames());
    }

    @ParameterizedTest
    @ValueSource(strings = {"de", "es", "ms", "tr", "zh"})
    void translationsOnlyHaveEnglishKeys(String language) throws IOException {
        assertTrue(
                bundle("en").stringPropertyNames().containsAll(bundle(language).stringPropertyNames()));
    }

    @Test
    void textsFollowThePlayersLanguage() {
        assertEquals("A blood night has begun in world.", begin(Locale.US));
        assertEquals("Eine Blutnacht hat in world begonnen.", begin(Locale.GERMANY));
        assertEquals("Eine Blutnacht hat in world begonnen.", begin(Locale.of("de", "AT")));
        assertEquals("血夜在世界 world 降临了...", begin(Locale.SIMPLIFIED_CHINESE));
    }

    @Test
    void missingTextsFallBack() {
        assertEquals("A blood night has begun in world.", begin(Locale.FRANCE));
        assertEquals(
                "Sleeping will not end this blood night.",
                render(Messages.message(Messages.NIGHT_SLEEP), Locale.of("ms", "MY")));
    }

    @Test
    void theFallbackIsConfigurable() {
        messages.fallback("de_DE");
        assertEquals("Eine Blutnacht hat in world begonnen.", begin(Locale.FRANCE));
    }

    @Test
    void miniMessageReachesTheTexts() {
        Component title = MiniMessage.miniMessage().deserialize("<red><lang:bloodnight.bossbar.title>");
        assertEquals("Blutnacht", render(title, Locale.GERMANY));
    }
}
