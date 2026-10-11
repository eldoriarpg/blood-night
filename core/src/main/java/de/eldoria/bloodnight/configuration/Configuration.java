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

import java.nio.file.Path;

public class Configuration {
    // TODO: Register default mobs on creation
    public static final Key<MobRegistry> MOBS = Key.builder(Path.of("mobs.yml"), MobRegistry::new).name("mobs").build();
    public static final Key<ItemRegistry> ITEMS = Key.builder(Path.of("items.yml"), ItemRegistry::new).name("items").build();
    public static final Key<GlobalDrops> GLOBAL_DROPS = Key.builder(Path.of("global_drops.yml"), GlobalDrops::defaultDrops).name("Global Drops").build();
    private static final Key<ConfigFile> MAIN = Key.builder(Path.of("config.yml"), ConfigFile::new).name("config").build();

    private final Configurations<ConfigFile> configurations;

    public Configuration(@NotNull Plugin plugin) {
        configurations = Configurations.builder(MAIN, new YamlDataFormat())
                .setBase(plugin.getDataFolder().toPath())
                .withClassLoader(plugin.getClass().getClassLoader())
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
        return configurations.secondary(key);
    }

    public MobRegistry mobs() {
        return configurations.secondary(MOBS);
    }

    public ItemRegistry items() {
        return configurations.secondary(ITEMS);
    }

    public GlobalDrops globalDrops() {
        return configurations.secondary(GLOBAL_DROPS);
    }
}
