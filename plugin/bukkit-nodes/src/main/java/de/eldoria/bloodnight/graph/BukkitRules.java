/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.reflect.Documentation;
import de.eldoria.graph.reflect.ReflectionRules;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;

import java.util.Collection;

/**
 * Reflection rules for the Bukkit API: event plumbing is skipped, and only ports of types the editor can work with
 * are derived, which keeps the palette of an entity to its useful properties.
 */
public final class BukkitRules {
    private BukkitRules() {}

    /**
     * @return the rules, without descriptions
     */
    public static ReflectionRules rules() {
        return rules(Documentation.NONE);
    }

    /**
     * @param documentation where derived nodes take their descriptions from
     * @return the rules
     */
    public static ReflectionRules rules(Documentation documentation) {
        return ReflectionRules.builder()
                .documentation(documentation)
                .excludeMethods("getHandlers", "getHandlerList", "getEventName", "isAsynchronous", "callEvent")
                .excludeDeclaredBy(Event.class)
                .acceptTypes(BukkitRules::accepted)
                .group(Entity.class, "Entity")
                .group(LivingEntity.class, "Living Entity")
                .group(Mob.class, "Mob")
                .group(Creeper.class, "Creeper")
                .group(Player.class, "Player")
                .group(Projectile.class, "Projectile")
                .build();
    }

    static boolean accepted(PortType type) {
        if (!(type instanceof PortType.Concrete concrete)) return false;
        Class<?> raw = concrete.raw();
        if (Collection.class.isAssignableFrom(raw)) {
            return concrete.arguments().size() == 1
                    && accepted(concrete.arguments().getFirst());
        }
        if (!concrete.arguments().isEmpty()) return false;
        if (raw.isPrimitive() || raw == String.class || raw.isEnum()) return true;
        if (Number.class.isAssignableFrom(raw) || raw == Boolean.class) return true;
        return BukkitTypes.KNOWN.stream().anyMatch(known -> known.isAssignableFrom(raw));
    }
}
