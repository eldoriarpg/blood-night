/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.living.spawn;

import de.eldoria.bloodnight.configuration.server.plugins.ForeignMobSettings;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Tells mobs other plugins own from the rest, so BloodNight leaves them alone: Citizens NPCs (their {@code NPC}
 * metadata), MythicMobs mobs (asked through MythicMobs itself when it runs), and entities carrying a configured data key.
 * A MythicMobs mob BloodNight spawned as one of its own mobs is no foreign mob.
 * <p>
 * MythicMobs is reached through reflection on its own class loader, so the plugin needs no MythicMobs classes to
 * build or run.
 */
public final class ForeignMobs {
    private final Supplier<ForeignMobSettings> settings;
    private final Supplier<@Nullable ClassLoader> mythicMobs;
    private volatile @Nullable MythicLookup mythic;

    /**
     * @param settings   the server's foreign mob settings
     * @param mythicMobs the class loader of MythicMobs while it runs, else {@code null}
     */
    public ForeignMobs(Supplier<ForeignMobSettings> settings, Supplier<@Nullable ClassLoader> mythicMobs) {
        this.settings = settings;
        this.mythicMobs = mythicMobs;
    }

    /**
     * @return whether another plugin owns the entity
     */
    public boolean isForeign(Entity entity) {
        if (MobTags.isCustomMob(entity)) return false;
        if (entity.hasMetadata("NPC")) return true;
        MythicLookup lookup = mythic();
        if (lookup != null && lookup.isMythicMob(entity)) return true;
        var data = entity.getPersistentDataContainer();
        for (String marker : settings.get().markerKeys()) {
            NamespacedKey key = NamespacedKey.fromString(marker);
            if (key != null && data.has(key)) return true;
        }
        return false;
    }

    private @Nullable MythicLookup mythic() {
        MythicLookup lookup = mythic;
        if (lookup != null) return lookup;
        ClassLoader loader = mythicMobs.get();
        if (loader == null) return null;
        lookup = MythicLookup.create(loader);
        mythic = lookup;
        return lookup;
    }

    /**
     * @return whether the entity may become a BloodNight mob
     */
    public boolean mayWrap(Entity entity) {
        return settings.get().wrap() || !isForeign(entity);
    }

    /**
     * @return whether blood night rules apply to the entity
     */
    public boolean ruled(Entity entity) {
        return settings.get().rules() || !isForeign(entity);
    }

    /**
     * {@code MythicBukkit.inst().getMobManager().isMythicMob(entity)}, looked up once.
     */
    private record MythicLookup(Method instance, Method mobManager, Method isMythicMob) {
        static @Nullable MythicLookup create(ClassLoader loader) {
            try {
                Class<?> bukkit = Class.forName("io.lumine.mythic.bukkit.MythicBukkit", true, loader);
                Method instance = bukkit.getMethod("inst");
                Method mobManager = bukkit.getMethod("getMobManager");
                Method isMythicMob = mobManager.getReturnType().getMethod("isMythicMob", Entity.class);
                return new MythicLookup(instance, mobManager, isMythicMob);
            } catch (ReflectiveOperationException | LinkageError e) {
                return null;
            }
        }

        boolean isMythicMob(Entity entity) {
            try {
                Object manager = mobManager.invoke(instance.invoke(null));
                return Boolean.TRUE.equals(isMythicMob.invoke(manager, entity));
            } catch (ReflectiveOperationException e) {
                return false;
            }
        }
    }
}
