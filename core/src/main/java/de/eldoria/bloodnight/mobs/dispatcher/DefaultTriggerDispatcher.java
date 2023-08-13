package de.eldoria.bloodnight.mobs.dispatcher;

import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.nodes.dispatching.TriggerData;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.OnDeathNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.OnExplosionPrimeNode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class listens to events, wraps their data into a {@link TriggerData} and trigger corresponding {@link TriggerNode}.
 * This class only covers predefined trigger nodes.
 * Custom nodes will need to create their own trigger dispatcher.
 */
public class DefaultTriggerDispatcher extends TriggerDispatcher {

    public DefaultTriggerDispatcher(MobCoordinator coordinator) {
        super(coordinator);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        trigger(event.getEntity(), TriggerData.forNode(OnDeathNode.class, event));
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        trigger(event.getEntity(), TriggerData.forNode(OnExplosionPrimeNode.class, event));
    }

}
