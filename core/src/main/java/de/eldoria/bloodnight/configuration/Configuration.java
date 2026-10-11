/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration;

import de.eldoria.bloodnight.configuration.elements.GlobalDrops;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mobs.MobRegistry;
import de.eldoria.jacksonbukkit.JacksonPaper;
import dev.chojo.ocular.Configurations;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.exceptions.ConfigurationException;
import dev.chojo.ocular.key.Key;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * All configuration files of the plugin.
 * <p>
 * Files holding what users wrote (mobs, items, macros) use strict keys: a file that cannot be read is never replaced
 * by a default. It is reported in {@link #problems()}, treated as empty and left untouched until it reads again on a
 * later {@link #reload()}. Keys are reloaded one by one, so a broken file does not stop the others from reloading.
 */
public class Configuration {
    // TODO: Register default mobs on creation
    public static final Key<MobRegistry> MOBS = Key.builder(Path.of("mobs.yml"), MobRegistry::new)
            .name("mobs")
            .strict()
            .build();
    public static final Key<ItemRegistry> ITEMS = Key.builder(Path.of("items.yml"), ItemRegistry::new)
            .name("items")
            .strict()
            .build();
    public static final Key<GlobalDrops> GLOBAL_DROPS = Key.builder(
                    Path.of("global_drops.yml"), GlobalDrops::defaultDrops)
            .name("Global Drops")
            .build();
    public static final Key<MacroFile> MACROS = Key.builder(Path.of("macros.yml"), MacroFile::new)
            .name("macros")
            .strict()
            .build();
    private static final Key<ConfigFile> MAIN =
            Key.builder(Path.of("config.yml"), ConfigFile::new).name("config").build();
    private static final List<Key<?>> USER_DATA = List.of(MOBS, ITEMS, MACROS);

    private final Configurations<ConfigFile> configurations;
    private final Set<Key<?>> worlds = new LinkedHashSet<>();
    private final Map<String, String> problems = new LinkedHashMap<>();

    public Configuration(@NotNull Plugin plugin) {
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
        return configurations.main();
    }

    public WorldSettings worldConfig(World world) {
        Key<WorldSettings> key = Key.builder(Path.of("worlds", world.getUID() + ".yml"), WorldSettings::new)
                .name(world.getName() + " Configuration")
                .build();
        worlds.add(key);
        return configurations.secondary(key);
    }

    public MobRegistry mobs() {
        return userData(MOBS, MobRegistry::new);
    }

    public ItemRegistry items() {
        return userData(ITEMS, ItemRegistry::new);
    }

    public MacroFile macros() {
        return userData(MACROS, MacroFile::new);
    }

    public GlobalDrops globalDrops() {
        return configurations.secondary(GLOBAL_DROPS);
    }

    /**
     * Reads every loaded configuration file again. User data files that do not read are left as they are and
     * reported; the other files still reload.
     */
    public void reload() {
        problems.clear();
        if (configurations.loaded(MAIN)) configurations.reload(MAIN);
        if (configurations.loaded(GLOBAL_DROPS)) configurations.reload(GLOBAL_DROPS);
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
    public Map<String, String> problems() {
        return Collections.unmodifiableMap(problems);
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
