package de.eldoria.bloodnight.mobs.dispatcher.impl;

import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.mobs.dispatcher.TriggerDispatcher;
import de.eldoria.bloodnight.nodes.dispatching.TriggerData;
import de.eldoria.bloodnight.nodes.trigger.impl.events.explosion.OnExplosionNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.explosion.OnExplosionPrimeNode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class ExplosionDispatcher extends TriggerDispatcher {
    public ExplosionDispatcher(MobCoordinator coordinator) {
        super(coordinator);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        trigger(event.getEntity(), TriggerData.forNode(OnExplosionPrimeNode.class, event));
    }

    @EventHandler
    public void onExplosionEvent(EntityExplodeEvent event) {
        trigger(event.getEntity(), TriggerData.forNode(OnExplosionNode.class, event));
    }
}
