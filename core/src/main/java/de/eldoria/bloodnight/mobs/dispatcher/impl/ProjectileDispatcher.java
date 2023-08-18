package de.eldoria.bloodnight.mobs.dispatcher.impl;

import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.mobs.dispatcher.TriggerDispatcher;
import de.eldoria.bloodnight.nodes.dispatching.TriggerData;
import de.eldoria.bloodnight.nodes.trigger.impl.events.projectile.OnProjectileHit;
import de.eldoria.bloodnight.nodes.trigger.impl.events.projectile.OnProjectileShoot;
import de.eldoria.eldoutilities.entities.projectiles.ProjectileSender;
import de.eldoria.eldoutilities.entities.projectiles.ProjectileUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileDispatcher extends TriggerDispatcher {
    public ProjectileDispatcher(MobCoordinator coordinator) {
        super(coordinator);
    }

    @EventHandler
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        ProjectileSender projectileSource = ProjectileUtil.getProjectileSource(event.getEntity());
        if (projectileSource.isEntity()) {
            trigger(projectileSource.getEntity(), TriggerData.forNode(OnProjectileShoot.class, event));
        }
    }

        @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        ProjectileSender projectileSource = ProjectileUtil.getProjectileSource(event.getEntity());
        if (projectileSource.isEntity()) {
            trigger(event.getEntity(), TriggerData.forNode(OnProjectileHit.class, event));
        }
    }
}
