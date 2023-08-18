package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * This class handles damage events for custom mobs with extensions.
 * It listens for entity damage events and performs necessary actions based on the type of damage and the entity involved.
 */
public class DamageHandler implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onExtensionDamage(EntityDamageEvent event) {
        if (MobTags.isExtension(event.getEntity())) handleExtensionDamage(event);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBaseDamage(EntityDamageEvent event) {
        if (MobTags.isExtended(event.getEntity())) handleBaseDamage(event);
    }

    private void handleExtensionDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        Optional<UUID> baseId = MobTags.getBase(entity);
        if (baseId.isEmpty()) return;
        if (!(entity instanceof LivingEntity extension)) return;
        // Forward damage from extension over to base mob
        if (!(entity.getServer().getEntity(baseId.get()) instanceof LivingEntity base)) return;

        if (event instanceof EntityDamageByEntityEvent byEntity) {
            base.damage(event.getDamage(), byEntity.getDamager());
        } else {
            base.damage(event.getDamage());
        }
        event.setCancelled(true);
        // Sync damage from base to extension
        extension.setHealth(base.getHealth());
        entity.playEffect(EntityEffect.HURT);
    }

    private void handleBaseDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        Optional<UUID> extensionId = MobTags.getExtension(entity);
        if (!(entity instanceof LivingEntity base)) return;

        if (extensionId.isEmpty()) return;
        if (!(entity.getServer().getEntity(extensionId.get()) instanceof LivingEntity extension)) return;

        // We assume that the damage won't change anymore
        extension.setHealth(base.getHealth() - event.getFinalDamage());
        extension.playEffect(EntityEffect.HURT);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        var entity = event.getEntity();
        if (!MobTags.isCustomMob(entity)) return;
        Optional<UUID> extensionId = MobTags.getExtension(entity);
        if (extensionId.isEmpty()) return;
        // Kill extension when the base dies.
        if (entity.getServer().getEntity(extensionId.get()) instanceof LivingEntity extension) {
            extension.damage(extension.getHealth());
        }
    }
}
