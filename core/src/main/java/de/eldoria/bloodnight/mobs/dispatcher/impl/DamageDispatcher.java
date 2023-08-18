package de.eldoria.bloodnight.mobs.dispatcher.impl;

import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.mobs.dispatcher.TriggerDispatcher;
import de.eldoria.bloodnight.nodes.dispatching.TriggerData;
import de.eldoria.bloodnight.nodes.trigger.impl.events.damage.OnDamageByEntityNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.damage.OnDamageByPlayerNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.damage.OnDamageNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.damage.OnDeathNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.damage.OnHitNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.kill.OnEntityKillNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.kill.OnKillNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.kill.OnPlayerKillNode;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * A {@link TriggerDispatcher} which only handles kills of entities
 */
public class DamageDispatcher extends TriggerDispatcher {
    public DamageDispatcher(MobCoordinator coordinator) {
        super(coordinator);
    }

    /**
     * This handler is purely for observing dealt damage to entities to notice who killed it.
     *
     * @param event damage event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onKill(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        // Check whether entity will be killed.
        if (event.getFinalDamage() >= living.getHealth()) {
            trigger(event.getDamager(), TriggerData.forNode(OnKillNode.class, event));
            if (event.getEntity() instanceof Player) {
                trigger(event.getDamager(), TriggerData.forNode(OnPlayerKillNode.class, event));
            } else {
                trigger(event.getDamager(), TriggerData.forNode(OnEntityKillNode.class, event));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        // We do not care about non CustomMob damage here
        if (!MobTags.isCustomMob(event.getEntity())) return;
        // Damage to extensions is handled somewhere else
        if (MobTags.isExtension(event.getEntity())) return;

        if (event instanceof EntityDamageByEntityEvent byEntity) {
            if (byEntity.getDamager().getType() == EntityType.PLAYER) {
                trigger(event.getEntity(), TriggerData.forNode(OnDamageByPlayerNode.class, byEntity));
            } else {
                trigger(event.getEntity(), TriggerData.forNode(OnDamageByEntityNode.class, byEntity));
            }
        } else {
            trigger(event.getEntity(), TriggerData.forNode(OnDamageNode.class, event));
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        trigger(event.getDamager(), TriggerData.forNode(OnHitNode.class, event));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent event) {
        trigger(event.getEntity(), TriggerData.forNode(OnDeathNode.class, event));
    }
}
