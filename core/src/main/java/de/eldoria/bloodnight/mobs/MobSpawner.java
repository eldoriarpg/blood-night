package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.mob.CustomMob;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class MobSpawner implements Listener, Runnable {
    private final Configuration configuration;
    private final Queue<Entity> nextTick = new ArrayDeque<>();
    private final Queue<Entity> schedule = new ArrayDeque<>();
    private final MobCoordinator coordinator;

    public MobSpawner(Configuration configuration, MobCoordinator coordinator) {
        this.configuration = configuration;
        this.coordinator = coordinator;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onSpawn(EntitySpawnEvent event) {
        // Wait one tick since there might be modification of freshly spawned mobs.
        schedule.add(event.getEntity());
    }

    void handleSpawnedMob(Entity entity) {
        WorldSettings worldSettings = configuration.worldConfig(entity.getWorld());
        Set<String> active = worldSettings.mobSettings().spawning().activeTypes();
        List<CustomMob> matching = configuration.mobs().getMatching(active, entity.getType());
        if(matching.isEmpty()) return;
        if (worldSettings.mobSettings().spawning().spawnPercentage() < ThreadLocalRandom.current().nextInt(100)) return;
        CustomMob customMob = matching.get(ThreadLocalRandom.current().nextInt(matching.size()));
        coordinator.register(entity, customMob);
    }

    @Override
    public void run() {
        while (!nextTick.isEmpty()) {
            handleSpawnedMob(nextTick.poll());
        }
        nextTick.addAll(schedule);
        schedule.clear();
    }
}
