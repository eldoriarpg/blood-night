package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.nodes.dispatching.TriggerData;
import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class MobCoordinator {
    private final Map<Integer, CustomMob> mobs = new HashMap<>();

    /**
     * Dispatch a trigger onto an entity if it is a custom mob.
     *
     * @param entity  entity
     * @param trigger trigger data
     */
    public void trigger(Entity entity, TriggerData<?> trigger) {
        CustomMob customMob = mobs.get(entity.getEntityId());
        if (customMob == null) return;
        customMob.nodes().dispatch(trigger);
    }

    public void register(Entity entity, CustomMob customMob) {

    }
}
