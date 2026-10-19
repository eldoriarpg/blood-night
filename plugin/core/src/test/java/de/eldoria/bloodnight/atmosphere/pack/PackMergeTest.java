/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere.pack;

import de.eldoria.bloodnight.mob.attributes.MobName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackMergeTest {
    private static final PackMerge PACK = PackMerge.of(Map.of(
            "pack.mcmeta", bytes("{}"),
            "assets/bloodnight/post_effect/blood.json", bytes("{\"passes\": []}")));

    @TempDir
    Path plugins;

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static PackMerge.Target target(String plugin) {
        return PackMerge.TARGETS.stream()
                .filter(target -> target.plugin().equals(plugin))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void zipPluginsGetTheSameZipEveryTime() throws IOException {
        assertTrue(PACK.install(plugins, target("Nexo")));
        assertFalse(PACK.install(plugins, target("Nexo")));

        assertArrayEquals(PACK.zip(), Files.readAllBytes(plugins.resolve("Nexo/pack/external_packs/BloodNight.zip")));
    }

    @Test
    void folderPluginsGetTheAssets() throws IOException {
        assertTrue(PACK.install(plugins, target("ItemsAdder")));

        Path root = plugins.resolve("ItemsAdder/contents/bloodnight/resourcepack");
        assertTrue(Files.exists(root.resolve("assets/bloodnight/post_effect/blood.json")));
        assertFalse(Files.exists(root.resolve("pack.mcmeta")));
    }

    @Test
    void mobNamesGoIntoTheLanguageFilesBesideWhatIsThere() throws IOException {
        PackMerge pack = PackMerge.of(Map.of(
                        "pack.mcmeta", bytes("{}"),
                        "assets/bloodnight/lang/de_de.json", bytes("{\"bloodnight.night\": \"Blutnacht\"}")))
                .withMobNames(
                        Map.of("tank", new MobName("<red>", Map.of("de", "Panzer", "en", "Tank", "pt_br", "Tanque"))));
        assertTrue(pack.install(plugins, target("ItemsAdder")));

        Path lang = plugins.resolve("ItemsAdder/contents/bloodnight/resourcepack/assets/bloodnight/lang");
        String german = Files.readString(lang.resolve("de_de.json"));
        assertTrue(german.contains("\"bloodnight.night\" : \"Blutnacht\""), german);
        assertTrue(german.contains("\"bloodnight.mob.tank\" : \"Panzer\""), german);
        assertTrue(Files.readString(lang.resolve("en_us.json")).contains("Tank"));
        assertTrue(Files.readString(lang.resolve("pt_br.json")).contains("Tanque"));
    }

    @Test
    void languageCodesBecomeMinecraftLocales() {
        assertEquals("zh_cn", PackMerge.minecraftLocale("zh"));
        assertEquals("pt_br", PackMerge.minecraftLocale("pt_br"));
        assertEquals("nl_nl", PackMerge.minecraftLocale("nl"));
    }

    @Test
    void onlyInstalledPackPluginsAreTargets() {
        assertEquals(List.of(target("Oraxen")), PackMerge.installed(name -> name.equals("Oraxen")));
    }

    @Test
    void theBundledPackHoldsTheShader() {
        assertTrue(
                new String(PackMerge.bundled().zip(), StandardCharsets.ISO_8859_1).contains("post_effect/blood.json"));
    }
}
