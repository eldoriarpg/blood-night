package de.eldoria.bloodnight.graph;

import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.reflect.ReflectionRules;
import org.bukkit.event.Event;

import java.util.Collection;

/**
 * Reflection rules for the Bukkit API: event plumbing is skipped, and only ports of types the editor can work with
 * are derived, which keeps the palette of an entity to its useful properties.
 */
public final class BukkitRules {
    private BukkitRules() {
    }

    /**
     * @return the rules
     */
    public static ReflectionRules rules() {
        return ReflectionRules.builder()
                .excludeMethods("getHandlers", "getHandlerList", "getEventName", "isAsynchronous", "callEvent")
                .excludeDeclaredBy(Event.class)
                .acceptTypes(BukkitRules::accepted)
                .build();
    }

    static boolean accepted(PortType type) {
        if (!(type instanceof PortType.Concrete concrete)) return false;
        Class<?> raw = concrete.raw();
        if (Collection.class.isAssignableFrom(raw)) {
            return concrete.arguments().size() == 1 && accepted(concrete.arguments().getFirst());
        }
        if (!concrete.arguments().isEmpty()) return false;
        if (raw.isPrimitive() || raw == String.class || raw.isEnum()) return true;
        if (Number.class.isAssignableFrom(raw) || raw == Boolean.class) return true;
        return BukkitTypes.KNOWN.stream().anyMatch(known -> known.isAssignableFrom(raw));
    }
}
