package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.nodes.dispatching.TriggerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Class which holds all custom mobs.
 * Responsible for dispatching event triggers to the correct custom mob.
 */
public class MobCoordinator {
    /**
     * Maps which references the {@link Entity#getEntityId()} and holds the custom mob assigned to id.
     */
    private final Map<Integer, CustomMob> mobs = new HashMap<>();

    private final Configuration configuration;

    public MobCoordinator(Configuration configuration) {
        this.configuration = configuration;
    }

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

    /**
     * Registers the custom mob on the provided entity.
     * Will create a living entity of the custom mob using {@link CustomMob#createLiving(LivingEntity, Configuration)}
     * If this mob is already registered, it will be changed to the new custom mob.
     *
     * @param entity    the entity to assign the mob to
     * @param customMob the custom mob assigned to the entity.
     */
    public void register(LivingEntity entity, CustomMob customMob) {
        mobs.put(entity.getEntityId(), customMob.createLiving(entity, configuration));
    }
}
