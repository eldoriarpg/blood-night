package de.eldoria.bloodnight.mobs.dispatcher;

import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.nodes.dispatching.TriggerData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;

/**
 * Basic Trigger dispatcher.
 * Holds a {@link MobCoordinator} and provides delegates to the core functionality.
 */
public class TriggerDispatcher implements Listener {
    private final MobCoordinator coordinator;

    public TriggerDispatcher(MobCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    public void trigger(Entity entity, TriggerData<?> trigger) {
        coordinator.trigger(entity, trigger);
    }
}
