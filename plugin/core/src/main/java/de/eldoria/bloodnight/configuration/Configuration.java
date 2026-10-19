/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration;

import de.eldoria.bloodnight.configuration.world.WorldSettings;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.living.MobRegistry;
import de.eldoria.bloodnight.loot.LootTables;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.graph.model.Macro;
import de.eldoria.jacksonbukkit.JacksonPaper;
import dev.chojo.ocular.Configurations;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.exceptions.ConfigurationException;
import dev.chojo.ocular.key.Key;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * All configuration files of the plugin.
 * <p>
 * Files holding what users wrote (mobs, items, macros, loot tables) use strict keys: a file that cannot be read is never replaced
 * by a default. It is reported in {@link #problems()}, treated as empty and left untouched until it reads again on a
 * later {@link #reload()}. Keys are reloaded one by one, so a broken file does not stop the others from reloading.
 * <p>
 * Settings files (the main configuration, worlds) are written back once read, so options added by a
 * newer version appear in existing files with their defaults. User data files are never written back.
 * <p>
 * Listeners read the configuration from every region thread on Folia. A file is read once and kept until the next
 * reload or replace; reading, writing and reloading happen under this object's lock, handing out what was read does
 * not.
 */
public class Configuration {
    public static final Key<MobRegistry> MOBS = Key.builder(Path.of("mobs.yml"), MobRegistry::new)
            .name("mobs")
            .strict()
            .build();
    public static final Key<ItemRegistry> ITEMS = Key.builder(Path.of("items.yml"), ItemRegistry::new)
            .name("items")
            .strict()
            .build();
    public static final Key<LootTables> LOOT_TABLES = Key.builder(Path.of("loot_tables.yml"), LootTables::new)
            .name("loot tables")
            .strict()
            .build();
    public static final Key<MacroFile> MACROS = Key.builder(Path.of("macros.yml"), MacroFile::new)
            .name("macros")
            .strict()
            .build();
    private static final Key<ConfigFile> MAIN =
            Key.builder(Path.of("config.yml"), ConfigFile::new).name("config").build();
    private static final List<Key<?>> USER_DATA = List.of(MOBS, ITEMS, MACROS, LOOT_TABLES);

    public static final String MISSING = "missing";
    private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private final Configurations<ConfigFile> configurations;
    private final Set<Key<?>> worlds = new LinkedHashSet<>();
    private final Set<Key<?>> completed = new HashSet<>();
    private final Map<String, String> problems = new LinkedHashMap<>();
    private final Map<Object, Object> cache = new ConcurrentHashMap<>();

    public Configuration(Plugin plugin) {
        this(plugin.getDataFolder().toPath(), plugin.getClass().getClassLoader());
    }

    /**
     * @param base        directory holding the files
     * @param classLoader class loader that sees the configuration classes
     */
    Configuration(Path base, ClassLoader classLoader) {
        configurations = Configurations.builder(MAIN, new YamlDataFormat())
                .setBase(base)
                .withClassLoader(classLoader)
                .addModule(JacksonPaper.builder().build())
                .build();
    }

    public ConfigFile main() {
        return cached(MAIN, () -> {
            ConfigFile main = configurations.main();
            complete(MAIN);
            return main;
        });
    }

    public WorldSettings worldConfig(World world) {
        return cached(world.getUID(), () -> settings(worldKey(world)));
    }

    /**
     * Reads a file once and keeps it until the next reload or replace: readers on any thread get it without a lock, a
     * file is read under the lock.
     */
    @SuppressWarnings("unchecked")
    private <V> V cached(Object key, Supplier<V> read) {
        Object known = cache.get(key);
        if (known != null) return (V) known;
        synchronized (this) {
            known = cache.get(key);
            if (known != null) return (V) known;
            V value = read.get();
            cache.put(key, value);
            return value;
        }
    }

    private Key<WorldSettings> worldKey(World world) {
        Key<WorldSettings> key = Key.builder(Path.of("worlds", world.getUID() + ".yml"), WorldSettings::new)
                .name(world.getName() + " Configuration")
                .build();
        worlds.add(key);
        return key;
    }

    /**
     * Copies every settings and user data file that exists to {@code backups/<time>/}.
     *
     * @return the backup's directory
     */
    public synchronized Path backup() throws IOException {
        Path base = configurations.base();
        Path target = base.resolve("backups").resolve(BACKUP_TIME.format(LocalDateTime.now()));
        for (String file : List.of("config.yml", "mobs.yml", "items.yml", "macros.yml", "loot_tables.yml", "worlds")) {
            Path source = base.resolve(file);
            if (!Files.exists(source)) continue;
            try (Stream<Path> files = Files.walk(source)) {
                for (Path path : files.filter(Files::isRegularFile).toList()) {
                    Path copy = target.resolve(base.relativize(path));
                    Files.createDirectories(Objects.requireNonNull(copy.getParent()));
                    Files.copy(path, copy);
                }
            }
        }
        return target;
    }

    /**
     * Hashes the files an editor session replaces, to tell later whether they changed: {@code config.yml},
     * {@code mobs.yml}, {@code items.yml}, {@code macros.yml}, {@code loot_tables.yml} and the files of the given
     * worlds.
     *
     * @param worlds the worlds whose settings are covered
     * @return SHA-256 per file, by path relative to the data folder; {@value #MISSING} for a file that does not exist
     */
    public synchronized Map<String, String> fileHashes(Collection<? extends World> worlds) throws IOException {
        Path base = configurations.base();
        List<String> files =
                new ArrayList<>(List.of("config.yml", "mobs.yml", "items.yml", "macros.yml", "loot_tables.yml"));
        worlds.forEach(world -> files.add("worlds/" + world.getUID() + ".yml"));
        Map<String, String> hashes = new TreeMap<>();
        for (String file : files) {
            Path path = base.resolve(file);
            hashes.put(file, Files.exists(path) ? sha256(Files.readAllBytes(path)) : MISSING);
        }
        return hashes;
    }

    private static String sha256(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Replaces the mobs and writes {@code mobs.yml}.
     */
    public void replaceMobs(Collection<CustomMob> mobs) {
        MobRegistry registry = new MobRegistry();
        mobs.forEach(registry::add);
        replace(MOBS, registry);
    }

    /**
     * Replaces the items and writes {@code items.yml}.
     */
    public void replaceItems(ItemRegistry items) {
        replace(ITEMS, items);
    }

    /**
     * Replaces the macros and writes {@code macros.yml}.
     */
    public void replaceMacros(List<Macro> macros) {
        replace(MACROS, new MacroFile(macros));
    }

    /**
     * Replaces the server-wide settings and writes {@code config.yml}.
     */
    public void replaceMain(ConfigFile main) {
        replace(MAIN, main);
    }

    /**
     * Replaces the loot tables and writes {@code loot_tables.yml}.
     */
    public void replaceLootTables(LootTables tables) {
        replace(LOOT_TABLES, tables);
    }

    /**
     * Replaces a world's settings and writes its file.
     */
    public void replaceWorld(World world, WorldSettings settings) {
        replace(worldKey(world), settings);
    }

    private synchronized <V> void replace(Key<V> key, V value) {
        cache.clear();
        problems.remove(key.path().toString());
        configurations.replace(key, value);
        configurations.save(key);
        completed.add(key);
    }

    public MobRegistry mobs() {
        return cached(MOBS, () -> userData(MOBS, MobRegistry::new));
    }

    public ItemRegistry items() {
        return cached(ITEMS, () -> userData(ITEMS, ItemRegistry::new));
    }

    public MacroFile macros() {
        return cached(MACROS, () -> userData(MACROS, MacroFile::new));
    }

    public LootTables lootTables() {
        return cached(LOOT_TABLES, () -> userData(LOOT_TABLES, LootTables::new));
    }

    /**
     * Reads every loaded configuration file again. User data files that do not read are left as they are and
     * reported; the other files still reload.
     */
    public synchronized void reload() {
        cache.clear();
        problems.clear();
        completed.clear();
        if (configurations.loaded(MAIN)) configurations.reload(MAIN);
        worlds.stream().filter(configurations::loaded).forEach(configurations::reload);
        for (Key<?> key : USER_DATA) {
            if (!configurations.loaded(key)) continue;
            try {
                configurations.reload(key);
            } catch (ConfigurationException e) {
                report(key, e);
            }
        }
    }

    /**
     * @return why user data files could not be read, by file; empty when all read
     */
    public synchronized Map<String, String> problems() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(problems));
    }

    private <V> V settings(Key<V> key) {
        V settings = configurations.secondary(key);
        complete(key);
        return settings;
    }

    private void complete(Key<?> key) {
        if (completed.add(key)) configurations.save(key);
    }

    private <V> V userData(Key<V> key, Supplier<V> empty) {
        if (problems.containsKey(key.path().toString())) return empty.get();
        try {
            return configurations.secondary(key);
        } catch (ConfigurationException e) {
            report(key, e);
            return empty.get();
        }
    }

    private void report(Key<?> key, ConfigurationException e) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        problems.put(key.path().toString(), cause.getMessage());
    }
}
