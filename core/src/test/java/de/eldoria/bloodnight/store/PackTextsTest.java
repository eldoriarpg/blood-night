/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackTextsTest {
    @TempDir
    Path dir;

    @Test
    void englishComesFromTheFallbacksOfTheDataPack() throws IOException {
        Path pack = Files.createDirectories(dir.resolve("data/bloodnight/advancement"));
        Files.writeString(
                pack.resolve("a.json"),
                "{\"display\":{\"title\":{\"translate\":\"k.title\",\"fallback\":\"Title\"},"
                        + "\"lore\":[{\"translate\":\"k.lore\",\"fallback\":\"Lore\"},{\"translate\":\"plain\"}]}}");

        assertEquals(Map.of("k.lore", "Lore", "k.title", "Title"), PackTexts.english(dir));
    }

    @Test
    void otherLanguagesMustCoverExactlyTheDataPacksKeys() throws IOException {
        Files.writeString(dir.resolve("de.json"), "{\"k.title\":\"Titel\",\"k.old\":\"Alt\"}");

        IllegalStateException error = assertThrows(
                IllegalStateException.class, () -> PackTexts.translated(dir, "de", Set.of("k.title", "k.lore")));

        assertTrue(error.getMessage().contains("missing [k.lore]"), error.getMessage());
        assertTrue(error.getMessage().contains("unused [k.old]"), error.getMessage());
    }
}
