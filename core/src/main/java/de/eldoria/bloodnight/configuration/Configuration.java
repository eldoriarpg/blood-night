package de.eldoria.bloodnight.configuration;

import de.eldoria.eldoutilities.config.ConfigKey;
import de.eldoria.eldoutilities.config.JacksonConfig;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class Configuration extends JacksonConfig<ConfigFile> {
    public Configuration(@NotNull Plugin plugin) {
        super(plugin, ConfigKey.defaultConfig(ConfigFile.class, ConfigFile::new));
    }

    public WorldSettings worldConfig(World world) {
        return secondary(ConfigKey.of(world.getName() + " Configuration", Path.of("worlds", world.getUID() + ".yml"), WorldSettings.class, WorldSettings::new));
    }
}
