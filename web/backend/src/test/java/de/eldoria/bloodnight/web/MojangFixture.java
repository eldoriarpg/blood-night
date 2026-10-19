/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import com.sun.net.httpserver.HttpServer;
import de.eldoria.bloodnight.web.assets.ItemAssets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Stands in for Mojang's servers: a version manifest knowing version {@code 1.0}, its metadata, asset index, German
 * language file and a small client jar with a flat item (a sword), a block item whose model inherits its texture
 * (stone), an item switching between models (a compass) and a specially drawn item whose base model gives its
 * texture as a sprite object (a chest).
 */
public final class MojangFixture implements AutoCloseable {
    static final byte[] SWORD_PNG = {(byte) 0x89, 'P', 'N', 'G', 1};
    public static final byte[] STONE_PNG = {(byte) 0x89, 'P', 'N', 'G', 2};
    public static final byte[] ZOMBIE_OGG = {'O', 'g', 'g', 'S', 1};
    private final HttpServer server;
    private final Map<String, byte[]> files = new ConcurrentHashMap<>();
    private final AtomicInteger jarDownloads = new AtomicInteger();
    private volatile boolean corruptJar;

    public MojangFixture() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            byte[] body = files.get(path);
            if (path.equals("/client.jar")) {
                jarDownloads.incrementAndGet();
                if (corruptJar) body = new byte[] {1, 2, 3};
            }
            if (body == null) {
                exchange.sendResponseHeaders(404, -1);
            } else {
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            }
            exchange.close();
        });
        server.start();
        byte[] jar = jar();
        byte[] german = """
                {"item.minecraft.diamond_sword":"Diamantschwert","block.minecraft.stone":"Stein","item.minecraft.compass":"Kompass","enchantment.minecraft.sharpness":"Schärfe","entity.minecraft.creeper":"Creeper","subtitles.entity.zombie.ambient":"Zombie stöhnt"}""".getBytes(StandardCharsets.UTF_8);
        String germanHash = sha1(german);
        files.put("/resources/" + germanHash.substring(0, 2) + "/" + germanHash, german);
        files.put("/client.jar", jar);
        byte[] sounds = """
                {"entity.zombie.ambient":{"subtitle":"subtitles.entity.zombie.ambient","sounds":["mob/zombie/say1",{"name":"mob/zombie/say2","weight":2}]},
                 "music.game":{"sounds":[{"name":"music.menu","type":"event"}]},
                 "ambient.cave":{"sounds":[{"name":"ambient/cave/cave1"}]}}""".getBytes(StandardCharsets.UTF_8);
        String soundsHash = sha1(sounds);
        files.put("/resources/" + soundsHash.substring(0, 2) + "/" + soundsHash, sounds);
        String zombieHash = sha1(ZOMBIE_OGG);
        files.put("/resources/" + zombieHash.substring(0, 2) + "/" + zombieHash, ZOMBIE_OGG);
        byte[] index = """
                {"objects":{"minecraft/lang/de_de.json":{"hash":"%s","size":%d},"minecraft/sounds.json":{"hash":"%s","size":%d},"minecraft/sounds/mob/zombie/say1.ogg":{"hash":"%s","size":5}}}""".formatted(germanHash, german.length, soundsHash, sounds.length, zombieHash)
                .getBytes(StandardCharsets.UTF_8);
        files.put("/index.json", index);
        byte[] meta = """
                {"downloads":{"client":{"url":"%s","sha1":"%s"}},"assetIndex":{"url":"%s","sha1":"%s"}}""".formatted(url("/client.jar"), sha1(jar), url("/index.json"), sha1(index))
                .getBytes(StandardCharsets.UTF_8);
        files.put("/1.0.json", meta);
        files.put("/manifest.json", """
                {"versions":[{"id":"1.0","url":"%s","sha1":"%s"}]}""".formatted(url("/1.0.json"), sha1(meta)).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @param cache the cache directory
     * @return assets fetched from this stand-in
     */
    public ItemAssets assets(Path cache) {
        return new ItemAssets(
                cache, HttpClient.newHttpClient(), URI.create(url("/manifest.json")), URI.create(url("/resources/")));
    }

    /**
     * @return how often the client jar was downloaded
     */
    public int jarDownloads() {
        return jarDownloads.get();
    }

    /**
     * @param corrupt whether the client jar is served with the wrong content, failing its sha1 check
     */
    public void corruptJar(boolean corrupt) {
        corruptJar = corrupt;
    }

    private String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    private static byte[] jar() {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("assets/minecraft/items/diamond_sword.json", text("""
                {"model":{"type":"minecraft:model","model":"minecraft:item/diamond_sword"}}"""));
        entries.put("assets/minecraft/items/stone.json", text("""
                {"model":{"type":"minecraft:model","model":"minecraft:block/stone"}}"""));
        entries.put("assets/minecraft/items/compass.json", text("""
                {"model":{"type":"minecraft:condition","on_false":{"type":"minecraft:range_dispatch","entries":[
                {"model":{"type":"minecraft:model","model":"minecraft:item/diamond_sword"}}]}}}"""));
        entries.put("assets/minecraft/items/chest.json", text("""
                {"model":{"type":"minecraft:select","cases":[],"fallback":{"type":"minecraft:special",
                "base":"minecraft:item/chest","model":{"type":"minecraft:chest"}}}}"""));
        entries.put("assets/minecraft/models/item/chest.json", text("""
                {"textures":{"particle":{"sprite":"minecraft:block/stone","force_translucent":true}}}"""));
        entries.put("assets/minecraft/models/item/diamond_sword.json", text("""
                {"parent":"minecraft:item/handheld","textures":{"layer0":"minecraft:item/diamond_sword"}}"""));
        entries.put("assets/minecraft/models/item/handheld.json", text("{}"));
        entries.put("assets/minecraft/models/block/stone.json", text("""
                {"parent":"minecraft:block/cube_all","textures":{"all":"minecraft:block/stone"}}"""));
        entries.put("assets/minecraft/models/block/cube_all.json", text("""
                {"parent":"block/cube","textures":{"particle":"#all","up":"#all"}}"""));
        entries.put("assets/minecraft/models/block/cube.json", text("{}"));
        entries.put("assets/minecraft/textures/item/diamond_sword.png", SWORD_PNG);
        entries.put("assets/minecraft/textures/block/stone.png", STONE_PNG);
        entries.put("assets/minecraft/particles/flame.json", text("""
                {"textures":["minecraft:flame","minecraft:missing"]}"""));
        entries.put("assets/minecraft/textures/particle/flame.png", STONE_PNG);
        entries.put("assets/minecraft/lang/en_us.json", text("""
                {"item.minecraft.diamond_sword":"Diamond Sword","block.minecraft.stone":"Stone","item.minecraft.compass":"Compass","block.minecraft.chest":"Chest","enchantment.minecraft.sharpness":"Sharpness","enchantment.minecraft.sharpness.desc":"More damage","entity.minecraft.creeper":"Creeper","biome.minecraft.plains":"Plains","subtitles.entity.zombie.ambient":"Zombie groans"}"""));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (var entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    private static byte[] text(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }

    private static String sha1(byte[] body) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(body));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
