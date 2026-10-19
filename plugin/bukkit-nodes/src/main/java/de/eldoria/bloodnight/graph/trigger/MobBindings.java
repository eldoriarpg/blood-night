/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.trigger;

import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.platform.Binding;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The graph-wide types of a mob's graph: {@link #SELF}, the mob's entity type, and {@link #EXTENSION}, the type of the
 * entity riding or carrying it. A graph is compiled for one mob, so Self and Extension give that mob's types, e.g. a
 * Creeper; checked on their own, as macros are, they fall back to a living entity and an entity.
 */
public final class MobBindings {
    /** The type of the mob a graph belongs to. */
    public static final String SELF = "SELF";
    /** The type of the entity riding or carrying the mob. */
    public static final String EXTENSION = "EXTENSION";

    private MobBindings() {}

    /**
     * @param entity    the mob's entity type
     * @param extension the extension's entity type, if the mob has one
     * @return the bindings for compiling the mob's graph; a type without an entity class keeps its fallback
     */
    public static Map<String, PortType> of(EntityType entity, @Nullable EntityType extension) {
        Map<String, PortType> bindings = new HashMap<>();
        Class<? extends Entity> self = entity.getEntityClass();
        if (self != null && LivingEntity.class.isAssignableFrom(self)) bindings.put(SELF, PortType.of(self));
        Class<? extends Entity> carrier = extension == null ? null : extension.getEntityClass();
        if (carrier != null) bindings.put(EXTENSION, PortType.of(carrier));
        return bindings;
    }

    public static Binding self() {
        return new Binding(PortType.of(LivingEntity.class), classes(true));
    }

    public static Binding extension() {
        return new Binding(PortType.of(Entity.class), classes(false));
    }

    private static List<PortType> classes(boolean living) {
        return Arrays.stream(EntityType.values())
                .map(EntityType::getEntityClass)
                .filter(Objects::nonNull)
                .filter(type -> !living || LivingEntity.class.isAssignableFrom(type))
                .distinct()
                .map(type -> (PortType) PortType.of(type))
                .toList();
    }
}
