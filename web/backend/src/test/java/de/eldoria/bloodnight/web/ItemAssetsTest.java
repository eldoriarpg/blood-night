/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.web.api.assets.ItemAsset;
import de.eldoria.bloodnight.web.api.assets.ItemIndex;
import de.eldoria.bloodnight.web.api.assets.SoundAsset;
import de.eldoria.bloodnight.web.api.assets.SoundIndex;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemAssetsTest {
    private MojangFixture mojang;

    @TempDir
    Path cache;

    @BeforeEach
    void setUp() throws IOException {
        mojang = new MojangFixture();
    }

    @AfterEach
    void tearDown() {
        mojang.close();
    }

    @Test
    void itemsGetTheirIconAndNames() throws Exception {
        ItemAssets assets = mojang.assets(cache);

        ItemIndex index = EditorApp.MAPPER.readValue(Files.readAllBytes(assets.items("1.0")), ItemIndex.class);

        assertEquals(
                Map.of(
                        "minecraft:diamond_sword",
                        new ItemAsset(
                                "item/diamond_sword", Map.of("en_us", "Diamond Sword", "de_de", "Diamantschwert")),
                        "minecraft:stone",
                        new ItemAsset("block/stone", Map.of("en_us", "Stone", "de_de", "Stein")),
                        "minecraft:compass",
                        new ItemAsset("item/diamond_sword", Map.of("en_us", "Compass", "de_de", "Kompass")),
                        "minecraft:chest",
                        new ItemAsset("block/stone", Map.of("en_us", "Chest"))),
                index.items());
        assertEquals(
                Map.of("minecraft:sharpness", Map.of("en_us", "Sharpness", "de_de", "Schärfe")),
                index.names().get("enchantment"));
        assertEquals(
                Map.of("minecraft:creeper", Map.of("en_us", "Creeper", "de_de", "Creeper")),
                index.names().get("entity_type"));
        assertEquals(
                Map.of("minecraft:plains", Map.of("en_us", "Plains")),
                index.names().get("biome"));
        assertArrayEquals(
                MojangFixture.STONE_PNG,
                Files.readAllBytes(assets.texture("1.0", "block", "stone.png").orElseThrow()));
    }

    @Test
    void aVersionIsFetchedOnceEvenWhenAskedAtOnce() throws Exception {
        ItemAssets assets = mojang.assets(cache);
        List<CompletableFuture<Path>> requests = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            requests.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return assets.items("1.0");
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }));
        }
        requests.forEach(CompletableFuture::join);
        assets.items("1.0");

        assertEquals(1, mojang.jarDownloads());
    }

    @Test
    void unknownVersionsAreReported() {
        ItemAssets assets = mojang.assets(cache);

        assertThrows(ItemAssets.UnknownVersion.class, () -> assets.items("9.9"));
        assertThrows(ItemAssets.UnknownVersion.class, () -> assets.items("../etc"));
    }

    @Test
    void aCorruptDownloadIsRefusedAndRetriedLater() throws Exception {
        ItemAssets assets = mojang.assets(cache);
        mojang.corruptJar(true);

        assertThrows(ItemAssets.Unavailable.class, () -> assets.items("1.0"));
        assertFalse(Files.exists(cache.resolve("1.0")));

        mojang.corruptJar(false);
        assertTrue(Files.isRegularFile(assets.items("1.0")));
    }

    @Test
    void texturesOutsideTheCacheCannotBeRead() throws Exception {
        ItemAssets assets = mojang.assets(cache);
        assets.items("1.0");

        assertTrue(assets.texture("1.0", "item", "diamond_sword.png").isPresent());
        assertTrue(assets.texture("1.0", "..", "items.json").isEmpty());
        assertTrue(assets.texture("1.0", "item", "../../items.json").isEmpty());
        assertTrue(assets.texture("1.0", "item", "missing.png").isEmpty());
    }

    @Test
    void soundsGetTheirGroupSubtitlesAndWhetherTheyPlay() throws Exception {
        ItemAssets assets = mojang.assets(cache);

        SoundIndex index = EditorApp.MAPPER.readValue(Files.readAllBytes(assets.sounds("1.0")), SoundIndex.class);

        assertEquals(
                Map.of(
                        "entity.zombie.ambient",
                        new SoundAsset("entity", Map.of("en_us", "Zombie groans", "de_de", "Zombie stöhnt"), true),
                        "music.game",
                        new SoundAsset("music", Map.of(), false),
                        "ambient.cave",
                        new SoundAsset("ambient", Map.of(), false)),
                index.sounds());
    }

    @Test
    void aSoundIsFetchedWhenFirstPlayedAndKept() throws Exception {
        ItemAssets assets = mojang.assets(cache);
        assets.sounds("1.0");

        Path zombie = assets.sound("1.0", "entity.zombie.ambient").orElseThrow();

        assertArrayEquals(MojangFixture.ZOMBIE_OGG, Files.readAllBytes(zombie));
        assertEquals(zombie, assets.sound("1.0", "entity.zombie.ambient").orElseThrow());
        assertTrue(assets.sound("1.0", "music.game").isEmpty());
        assertTrue(assets.sound("1.0", "../sound-files").isEmpty());
        assertTrue(assets.sound("2.0", "entity.zombie.ambient").isEmpty());
    }
}
