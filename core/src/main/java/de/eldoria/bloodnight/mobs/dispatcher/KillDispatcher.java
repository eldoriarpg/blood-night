package de.eldoria.bloodnight.mobs.dispatcher;

import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.nodes.dispatching.TriggerData;
import de.eldoria.bloodnight.nodes.trigger.impl.events.OnDeathNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.OnEntityKillNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.OnPlayerKillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillDispatcher extends TriggerDispatcher {
    public KillDispatcher(MobCoordinator coordinator) {
        super(coordinator);
    }

    /**
     * This handler is purely for observing dealt damage to entities to notice who killed it.
     *
     * @param event damage event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageDelegate(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        // Check if entity will be killed.
        if (event.getFinalDamage() >= living.getHealth()) {
            if (event.getEntity() instanceof Player) {
                trigger(event.getDamager(), TriggerData.forNode(OnPlayerKillNode.class, event));
            } else {
                trigger(event.getDamager(), TriggerData.forNode(OnEntityKillNode.class, event));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        trigger(event.getEntity(), TriggerData.forNode(OnDeathNode.class, event));
    }
}
