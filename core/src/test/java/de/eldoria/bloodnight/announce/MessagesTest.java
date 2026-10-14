/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
        return render(Messages.message(NightKind.NIGHT.key("begin"), Argument.string("world", "world")), locale);
    }

    private static Properties bundle(String language) throws IOException {
        try (InputStream stream =
                MessagesTest.class.getClassLoader().getResourceAsStream("messages/%s.properties".formatted(language))) {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return properties;
        }
    }

    private static final String COMMANDS = "bloodnight.command.";

    @Test
    void englishHasEveryText() throws IOException {
        Set<String> expected = new HashSet<>(Set.of(
                Messages.NIGHT_SLEEP,
                Messages.COMMAND_BLOCKED,
                NightKind.DRAGON.key("begin"),
                NightKind.DRAGON.key("end")));
        for (NightKind kind : List.of(NightKind.NIGHT, NightKind.TIDE, NightKind.ECLIPSE)) {
            for (String text : List.of("begin", "end", "joined", "left", "name")) expected.add(kind.key(text));
        }
        Set<String> announcements = new HashSet<>(bundle("en").stringPropertyNames());
        announcements.removeIf(key ->
                key.startsWith(COMMANDS) && !key.equals(Messages.COMMAND_BLOCKED) || key.startsWith("bloodnight.mob."));
        assertEquals(expected, announcements);
    }

    @Test
    void germanHasEveryCommandText() throws IOException {
        Set<String> english = new HashSet<>(bundle("en").stringPropertyNames());
        english.removeIf(key -> !key.startsWith(COMMANDS));
        assertTrue(bundle("de").stringPropertyNames().containsAll(english));
    }

    @Test
    void everyCommandTextTheCommandsUseExists() throws IOException {
        Pattern used = Pattern.compile("Replies\\.(?:send|text)\\(\\s*(?:sender,\\s*)?\"([a-z_.]+)\"(?!\\s*\\+)");
        Set<String> english = bundle("en").stringPropertyNames();
        try (Stream<Path> files = Files.list(Path.of("src/main/java/de/eldoria/bloodnight/command"))) {
            for (Path file : files.toList()) {
                Matcher matcher = used.matcher(Files.readString(file));
                while (matcher.find()) {
                    assertTrue(english.contains(COMMANDS + matcher.group(1)), file + ": " + matcher.group(1));
                }
            }
        }
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
        Component title = MiniMessage.miniMessage()
                .deserialize(
                        "<red><name>",
                        Placeholder.component("name", Component.translatable("bloodnight.eclipse.name")));
        assertEquals("Blutfinsternis", render(title, Locale.GERMANY));
    }
}
