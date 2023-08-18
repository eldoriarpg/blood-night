package de.eldoria.bloodnight.mobs.dispatcher.impl;

import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.mobs.dispatcher.TriggerDispatcher;
import de.eldoria.bloodnight.nodes.dispatching.TriggerData;
import de.eldoria.bloodnight.nodes.trigger.TriggerNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.OnTargetChangeNode;
import de.eldoria.bloodnight.nodes.trigger.impl.events.OnTeleportNode;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;

/**
 * This class listens to events, wraps their data into a {@link TriggerData} and trigger corresponding {@link TriggerNode}.
 * This class only covers predefined trigger nodes.
 * Custom nodes will need to create their own trigger dispatcher.
 */
public class StandardTriggerDispatcher extends TriggerDispatcher {

    public StandardTriggerDispatcher(MobCoordinator coordinator) {
        super(coordinator);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(EntityTeleportEvent event) {
        trigger(event.getEntity(), TriggerData.forNode(OnTeleportNode.class, event));
    }

    @EventHandler
    public void onTargetEvent(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() != null && MobTags.isCustomMob(event.getTarget())) {
            event.setCancelled(true);
            return;
        }

        if (!MobTags.isCustomMob(event.getEntity())) {
            return;
        }

        // Block that custom mobs target something else than players.
        // Otherwise, they will probably kill each other.
        if (event.getTarget() != null && event.getTarget().getType() != EntityType.PLAYER) {
            event.setCancelled(true);
            return;
        }
        trigger(event.getEntity(), TriggerData.forNode(OnTargetChangeNode.class, event));
    }
}
