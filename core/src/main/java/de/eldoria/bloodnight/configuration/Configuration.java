package de.eldoria.bloodnight.configuration;

import de.eldoria.bloodnight.configuration.elements.GlobalDrops;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mobs.MobRegistry;
import de.eldoria.eldoutilities.config.ConfigKey;
import de.eldoria.eldoutilities.config.JacksonConfig;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class Configuration extends JacksonConfig<ConfigFile> {
    // TODO: Register default mobs on creation
    public static final ConfigKey<MobRegistry> MOBS = ConfigKey.of("mobs", Path.of("mobs.yml"), MobRegistry.class, MobRegistry::new);
    public static final ConfigKey<ItemRegistry> ITEMS = ConfigKey.of("items", Path.of("items.yml"), ItemRegistry.class, ItemRegistry::new);
    public static final ConfigKey<GlobalDrops> GLOBAL_DROPS = ConfigKey.of("Global Drops", Path.of("global_drops.yml"), GlobalDrops.class, GlobalDrops::defaultDrops);

    public Configuration(@NotNull Plugin plugin) {
        super(plugin, ConfigKey.defaultConfig(ConfigFile.class, ConfigFile::new));
    }

    public WorldSettings worldConfig(World world) {
        return secondary(ConfigKey.of(world.getName() + " Configuration", Path.of("worlds", world.getUID() + ".yml"), WorldSettings.class, WorldSettings::new));
    }

    public MobRegistry mobs() {
        return secondary(MOBS);
    }

    public ItemRegistry items() {
        return secondary(ITEMS);
    }

    public GlobalDrops globalDrops(){
        return secondary(GLOBAL_DROPS);
    }
}
