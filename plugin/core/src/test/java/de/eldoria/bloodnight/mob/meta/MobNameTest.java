/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobNameTest {
    @Test
    void keepsOnlyTextsWithALanguage() {
        Map<String, String> texts = new LinkedHashMap<>();
        texts.put("DE", " Panzer ");
        texts.put("fr", " ");
        texts.put(" ", "Nobody");
        MobName name = new MobName(null, texts);
        assertEquals(Map.of("de", "Panzer"), name.texts());
        assertEquals("", name.style());
    }

    @Test
    void showsTheFallbackLanguageThenEnglishThenAny() {
        MobName name = new MobName("", Map.of("en", "Tank", "de", "Panzer", "tr", "Tank TR"));
        assertEquals("Panzer", name.text("de"));
        assertEquals("Tank", name.text("fr"));
        assertEquals("Panzer", new MobName("", Map.of("tr", "Tank TR", "de", "Panzer")).text("fr"));
        assertTrue(MobName.NONE.isEmpty());
        assertEquals("", MobName.NONE.miniMessage("tank", "en"));
    }

    @Test
    void rendersAsATranslatableTextWithItsStyle() {
        for (String text : List.of("Dragon's <red>Heir", "Back\\slash", "Plain")) {
            MobName name = new MobName("<dark_red>", Map.of("en", text));
            Component rendered = MiniMessage.miniMessage().deserialize(name.miniMessage("blood_dragon", "en"));
            TranslatableComponent translatable = rendered instanceof TranslatableComponent own
                    ? own
                    : (TranslatableComponent) rendered.children().getFirst();
            assertEquals("bloodnight.mob.blood_dragon", translatable.key(), text);
            assertEquals(text, translatable.fallback(), text);
            assertEquals(
                    NamedTextColor.DARK_RED, rendered.color() != null ? rendered.color() : translatable.color(), text);
        }
    }

    @Test
    void withoutTranslationEveryoneSeesTheFallbackLanguagesText() {
        MobName name = new MobName("<red>", Map.of("en", "Tank", "de", "Panzer <3"));
        assertEquals("<red>Panzer <3", name.miniMessage("tank", "de", false));
        assertEquals("<red>Tank", name.miniMessage("tank", "fr", false));
    }

    @Test
    void aWorldsNameWinsPerLanguageAndKeepsTheStyleWhenItHasNone() {
        MobName base = new MobName("<red>", Map.of("en", "Tank", "de", "Panzer"));
        MobName world = new MobName("", Map.of("de", "Kampfpanzer"));
        assertEquals(new MobName("<red>", Map.of("en", "Tank", "de", "Kampfpanzer")), world.over(base));
        assertEquals("<blue>", new MobName("<blue>", Map.of()).over(base).style());
    }
}
