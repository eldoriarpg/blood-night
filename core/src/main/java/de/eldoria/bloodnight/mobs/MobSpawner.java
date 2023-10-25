package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class MobSpawner implements Listener {
    private final Configuration configuration;
    private final MobCoordinator coordinator;
    private final Plugin plugin;

    public MobSpawner(Configuration configuration, MobCoordinator coordinator, Plugin plugin) {
        this.configuration = configuration;
        this.coordinator = coordinator;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawn(EntitySpawnEvent event) {
        // Wait one tick since there might be modification of freshly spawned mobs.
        var entity = event.getEntity();
        if (!(entity instanceof LivingEntity livingEntity)) return;
        entity.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> handleSpawnedMob(livingEntity), 1);
    }

    void handleSpawnedMob(LivingEntity entity) {
        if (MobTags.isExtended(entity)) return;

        WorldSettings worldSettings = configuration.worldConfig(entity.getWorld());
        Set<String> active = worldSettings.mobSettings().spawning().activeTypes();
        List<CustomMob> matching = configuration.mobs().getMatching(active, entity.getType());
        if(matching.isEmpty()) return;
        if (worldSettings.mobSettings().spawning().spawnPercentage() < ThreadLocalRandom.current().nextInt(100)) return;
        // TODO: This doesnt factor in the spawn percentage of matching active mobs itself.
        // TODO: Per mob spawn rate is not yet implemented.
        CustomMob customMob = matching.get(ThreadLocalRandom.current().nextInt(matching.size()));
        // TODO Building the mob with changed attributes, equipment, extensions etc is missing.
        coordinator.register(entity, customMob);
    }

}
