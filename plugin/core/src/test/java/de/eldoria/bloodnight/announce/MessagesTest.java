/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import de.eldoria.bloodnight.web.api.Language;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The texts the server renders on its own: night names and the blood curse's death message.
 */
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

    private static String eclipse(Locale locale) {
        return render(Component.translatable(NightKind.ECLIPSE.key("name")), locale);
    }

    private static Properties bundle(String language) throws IOException {
        try (InputStream stream = MessagesTest.class
                .getClassLoader()
                .getResourceAsStream("translations/%s.properties".formatted(language))) {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return properties;
        }
    }

    @Test
    void englishHasEveryText() throws IOException {
        Set<String> expected =
                new HashSet<>(Set.of(Messages.BLOOD_CURSE_DEATH, Messages.BLOOD_CURSE_DEATH + ".player"));
        for (NightKind kind : List.of(NightKind.NIGHT, NightKind.TIDE, NightKind.ECLIPSE))
            expected.add(kind.key("name"));
        assertEquals(expected, bundle("en").stringPropertyNames());
    }

    @ParameterizedTest
    @EnumSource(Language.class)
    void everyLanguageHasEveryText(Language language) throws IOException {
        assertEquals(
                bundle("en").stringPropertyNames(), bundle(language.language()).stringPropertyNames());
    }

    @Test
    void textsFollowThePlayersLanguage() {
        assertEquals("Blood Eclipse", eclipse(Locale.US));
        assertEquals("Blutfinsternis", eclipse(Locale.GERMANY));
        assertEquals("Blutfinsternis", eclipse(Locale.of("de", "AT")));
        assertEquals("血蚀", eclipse(Locale.SIMPLIFIED_CHINESE));
    }

    @Test
    void missingTextsFallBack() {
        assertEquals("Blood Eclipse", eclipse(Locale.ITALY));
    }

    @Test
    void theFallbackIsConfigurable() {
        messages.fallback("de_DE");
        assertEquals("Blutfinsternis", eclipse(Locale.ITALY));
    }

    @Test
    void miniMessageReachesTheTexts() {
        Component title = MiniMessage.miniMessage()
                .deserialize(
                        "<red><name>",
                        Placeholder.component("name", Component.translatable("bloodnight.eclipse.name")));
        assertEquals("Blutfinsternis", render(title, Locale.GERMANY));
    }

    @Test
    void theBloodCurseHasADeathMessageWithItsVictimAndKiller() {
        Component death = Component.translatable(
                Messages.BLOOD_CURSE_DEATH + ".player", Component.text("Steve"), Component.text("Zombie"));

        assertEquals("Steve was claimed by the blood night through Zombie", render(death, Locale.US));
        assertEquals("Steve wurde durch Zombie von der Blutnacht geholt", render(death, Locale.GERMANY));
    }
}
