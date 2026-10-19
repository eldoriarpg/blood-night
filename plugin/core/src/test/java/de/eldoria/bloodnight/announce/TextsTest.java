/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import de.eldoria.bloodnight.web.api.Language;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The chat texts: complete in English and German, styled, in each player's language, with the prefix in chat.
 */
class TextsTest {
    private static final Set<String> STYLES =
            Set.of("accent", "muted", "success", "warn", "error", "bold", "url", "cmd");
    private static final Pattern PLACEHOLDER = Pattern.compile("<([a-z_]+)>");
    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        Texts.create(MockBukkit.createMockPlugin(), "<dark_red>[BN]</dark_red> ", "en_US");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static Properties bundle(String locale) throws IOException {
        try (InputStream stream =
                TextsTest.class.getClassLoader().getResourceAsStream("messages_%s.properties".formatted(locale))) {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return properties;
        }
    }

    private static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    private static String begin(CommandSender receiver) {
        return plain(Texts.render(receiver, NightKind.NIGHT.key("begin"), Placeholder.unparsed("world", "world")));
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    void everyLanguageHasEveryText(Language language) throws IOException {
        assertEquals(
                bundle("en_US").stringPropertyNames(), bundle(language.name()).stringPropertyNames());
    }

    @Test
    void everyCommandTextTheCommandsUseExists() throws IOException {
        Pattern used = Pattern.compile("Replies\\.(?:send|text)\\(\\s*sender,\\s*\"([a-z_.]+)\"(?!\\s*\\+)");
        Set<String> english = bundle("en_US").stringPropertyNames();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/de/eldoria/bloodnight/command"))) {
            for (Path file :
                    files.filter(path -> path.toString().endsWith(".java")).toList()) {
                Matcher matcher = used.matcher(Files.readString(file));
                while (matcher.find()) {
                    assertTrue(
                            english.contains("bloodnight.command." + matcher.group(1)), file + ": " + matcher.group(1));
                }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    void everyTextResolvesWithoutLeftoverTags(Language language) throws IOException {
        Properties texts = bundle(language.name());
        PlayerMock player = server.addPlayer();
        player.setLocale(Locale.forLanguageTag(language.name().replace('_', '-')));
        for (String key : texts.stringPropertyNames()) {
            List<TagResolver> placeholders = new ArrayList<>();
            Matcher matcher = PLACEHOLDER.matcher(texts.getProperty(key));
            while (matcher.find()) {
                if (!STYLES.contains(matcher.group(1))) placeholders.add(Placeholder.unparsed(matcher.group(1), "x"));
            }
            String rendered = plain(Texts.render(player, key, placeholders.toArray(TagResolver[]::new)));
            assertFalse(rendered.contains("<"), key + ": " + rendered);
        }
    }

    @Test
    void textsFollowThePlayersLanguage() {
        PlayerMock player = server.addPlayer();
        player.setLocale(Locale.US);
        assertEquals("A blood night has begun in world.", begin(player));
        player.setLocale(Locale.GERMANY);
        assertEquals("Eine Blutnacht hat in world begonnen.", begin(player));
        player.setLocale(Locale.of("de", "AT"));
        assertEquals("Eine Blutnacht hat in world begonnen.", begin(player));
        player.setLocale(Locale.ITALY);
        assertEquals("A blood night has begun in world.", begin(player));
    }

    @Test
    void chatCarriesThePrefix() {
        PlayerMock player = server.addPlayer();
        Texts.sender().sendMessage(player, "bloodnight.command.store.nothing");

        Component message = player.nextComponentMessage();
        assertNotNull(message);
        assertEquals("[BN] Nothing to do.", plain(message));
    }

    @Test
    void commandsAreClickable() {
        Component text = Texts.render(server.getConsoleSender(), "bloodnight.command.editor.none_open");

        assertTrue(clicks(text).contains(ClickEvent.suggestCommand("/bloodnight editor")), text.toString());
    }

    @Test
    void changedSettingsApply() {
        Texts.create(MockBukkit.createMockPlugin(), "<gray>[X]", "de_DE");
        PlayerMock player = server.addPlayer();
        player.setLocale(Locale.ITALY);
        Texts.sender().sendMessage(player, "bloodnight.command.store.nothing");

        assertEquals("[X] Nichts zu tun.", plain(player.nextComponentMessage()));
    }

    private static List<ClickEvent> clicks(Component component) {
        List<ClickEvent> clicks = new ArrayList<>();
        if (component.clickEvent() != null) clicks.add(component.clickEvent());
        for (Component child : component.children()) clicks.addAll(clicks(child));
        return clicks;
    }
}
