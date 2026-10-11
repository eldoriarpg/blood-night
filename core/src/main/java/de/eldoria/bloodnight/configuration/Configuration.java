package de.eldoria.bloodnight.configuration;

import de.eldoria.bloodnight.configuration.elements.GlobalDrops;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mobs.MobRegistry;
import de.eldoria.jacksonbukkit.JacksonPaper;
import dev.chojo.ocular.Configurations;
import dev.chojo.ocular.dataformats.YamlDataFormat;
import dev.chojo.ocular.key.Key;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.yaml.YAMLMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * All configuration files of the plugin.
 * <p>
 * Files holding what users wrote (mobs, items, macros) are never reset: ocular replaces a file it cannot read with
 * its default, so these files are test-read first. A file that fails is reported in {@link #problems()}, treated as
 * empty and left untouched until it reads again on a later {@link #reload()}.
 */
public class Configuration {
    // TODO: Register default mobs on creation
    public static final Key<MobRegistry> MOBS = Key.builder(Path.of("mobs.yml"), MobRegistry::new).name("mobs").build();
    public static final Key<ItemRegistry> ITEMS = Key.builder(Path.of("items.yml"), ItemRegistry::new).name("items").build();
    public static final Key<GlobalDrops> GLOBAL_DROPS = Key.builder(Path.of("global_drops.yml"), GlobalDrops::defaultDrops).name("Global Drops").build();
    public static final Key<MacroFile> MACROS = Key.builder(Path.of("macros.yml"), MacroFile::new).name("macros").build();
    private static final Key<ConfigFile> MAIN = Key.builder(Path.of("config.yml"), ConfigFile::new).name("config").build();
    // TODO: Mark user data keys strict() and drop the test reads once ocular with strict keys is released
    private static final List<Key<?>> USER_DATA = List.of(MOBS, ITEMS, MACROS);

    private final Configurations<ConfigFile> configurations;
    private final YAMLMapper probe;
    private final Map<String, String> problems = new LinkedHashMap<>();

    public Configuration(@NotNull Plugin plugin) {
        this(plugin.getDataFolder().toPath(), plugin.getClass().getClassLoader());
    }

    /**
     * @param base        directory holding the files
     * @param classLoader class loader that sees the configuration classes
     */
    Configuration(Path base, ClassLoader classLoader) {
        JacksonPaper paper = JacksonPaper.builder().build();
        configurations = Configurations.builder(MAIN, new YamlDataFormat())
                .setBase(base)
                .withClassLoader(classLoader)
                .addModule(paper)
                .build();
        probe = YAMLMapper.builder().addModule(paper).build();
    }

    public ConfigFile main() {
        return configurations.main();
    }

    public WorldSettings worldConfig(World world) {
        Key<WorldSettings> key = Key.builder(Path.of("worlds", world.getUID() + ".yml"), WorldSettings::new)
                .name(world.getName() + " Configuration")
                .build();
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
     * Reads every configuration file again. User data files that do not read are left as they are and reported.
     */
    public void reload() {
        problems.clear();
        USER_DATA.forEach(this::probe);
        if (problems.isEmpty()) {
            configurations.reload();
            return;
        }
        configurations.reload(MAIN);
        if (configurations.loaded(GLOBAL_DROPS)) configurations.reload(GLOBAL_DROPS);
        for (Key<?> key : USER_DATA) {
            if (!problems.containsKey(key.path().toString()) && configurations.loaded(key)) configurations.reload(key);
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
        if (!configurations.loaded(key) && !probe(key)) return empty.get();
        return configurations.secondary(key);
    }

    /**
     * Test-reads a user data file.
     *
     * @return whether the file is absent or reads
     */
    private boolean probe(Key<?> key) {
        Path file = configurations.base().resolve(key.path());
        if (!Files.exists(file)) return true;
        try {
            probe.readValue(file.toFile(), key.configClazz());
            return true;
        } catch (JacksonException | IllegalArgumentException e) {
            problems.put(key.path().toString(), e.getMessage());
            return false;
        }
    }
}
