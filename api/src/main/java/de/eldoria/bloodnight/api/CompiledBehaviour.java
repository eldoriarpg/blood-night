package de.eldoria.bloodnight.api;

import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Behaviour of a custom mob, generated and compiled at runtime from its node graph.
 * <p>
 * Every entry point is a no-op by default; the generated class overrides those its graph uses. This is the only type
 * the plugin and generated code share.
 */
public interface CompiledBehaviour {
    /**
     * Called once per server tick.
     */
    default void onTick() {
    }

    /**
     * Called when the mob takes damage.
     *
     * @param event the damage event
     */
    default void onEntityDamage(EntityDamageEvent event) {
    }
}
