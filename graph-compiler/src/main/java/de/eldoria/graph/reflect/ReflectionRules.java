/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.reflect;

import de.eldoria.graph.definition.PortType;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Decides which methods become ports when nodes are derived by reflection.
 * <p>
 * Always skipped: members of {@link Object}, static, bridge and synthetic methods, and methods declaring checked
 * exceptions, which generated host methods could not propagate. Configurable: deprecated members, method names
 * (plumbing like {@code getHandlers}), declaring classes, and which port types are acceptable at all.
 */
public final class ReflectionRules {
    private final boolean excludeDeprecated;
    private final Set<String> excludedMethods;
    private final Set<Class<?>> excludedDeclarers;
    private final Predicate<PortType> types;
    private final Documentation documentation;
    private final Map<Class<?>, String> groups;

    private ReflectionRules(
            boolean excludeDeprecated,
            Set<String> excludedMethods,
            Set<Class<?>> excludedDeclarers,
            Predicate<PortType> types,
            Documentation documentation,
            Map<Class<?>, String> groups) {
        this.excludeDeprecated = excludeDeprecated;
        this.excludedMethods = Set.copyOf(excludedMethods);
        this.excludedDeclarers = Set.copyOf(excludedDeclarers);
        this.types = types;
        this.documentation = documentation;
        this.groups = Map.copyOf(groups);
    }

    /**
     * The group a property of a type is drawn in: the most general registered group type that the type is one of and
     * that already has the property, e.g. "Entity" for {@code getLocation} on a creeper and "Creeper" for
     * {@code getFuseTicks}.
     *
     * @param owner  the type the node reads or changes
     * @param method the property's getter or setter
     * @return the group's name, or null when the owner belongs to no group
     */
    @Nullable
    String group(Class<?> owner, Method method) {
        return levels(owner).stream()
                .filter(level -> method.getDeclaringClass().isAssignableFrom(level))
                .reduce((specific, general) -> general)
                .map(groups::get)
                .orElse(null);
    }

    /**
     * @param owner the type the node reads or changes
     * @return the names of the groups its properties fall into, the most specific first
     */
    List<String> groupOrder(Class<?> owner) {
        return levels(owner).stream().map(groups::get).toList();
    }

    private List<Class<?>> levels(Class<?> owner) {
        return groups.keySet().stream()
                .filter(level -> level.isAssignableFrom(owner))
                .sorted(Comparator.comparingLong((Class<?> level) -> groups.keySet().stream()
                                .filter(other -> other.isAssignableFrom(level))
                                .count())
                        .reversed())
                .toList();
    }

    /**
     * @return where derived nodes take their descriptions from
     */
    Documentation documentation() {
        return documentation;
    }

    /**
     * @return a builder that excludes deprecated members and accepts every type
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return the default rules
     */
    public static ReflectionRules defaults() {
        return builder().build();
    }

    /**
     * @param method a candidate getter or setter
     * @return whether the method may become a port
     */
    boolean accepts(Method method) {
        if (method.getDeclaringClass() == Object.class) return false;
        if (Modifier.isStatic(method.getModifiers()) || method.isBridge() || method.isSynthetic()) return false;
        if (excludeDeprecated && method.isAnnotationPresent(Deprecated.class)) return false;
        if (excludedMethods.contains(method.getName())) return false;
        if (excludedDeclarers.contains(method.getDeclaringClass())) return false;
        return Arrays.stream(method.getExceptionTypes()).noneMatch(ReflectionRules::checked);
    }

    /**
     * @param type type of a candidate port
     * @return whether a port of the type is wanted
     */
    boolean accepts(PortType type) {
        return !type.hasVariables() && types.test(type);
    }

    private static boolean checked(Class<?> exception) {
        return !RuntimeException.class.isAssignableFrom(exception) && !Error.class.isAssignableFrom(exception);
    }

    /**
     * Builder for {@link ReflectionRules}.
     */
    public static final class Builder {
        private final Set<String> excludedMethods = new HashSet<>();
        private final Set<Class<?>> excludedDeclarers = new HashSet<>();
        private boolean excludeDeprecated = true;
        private Predicate<PortType> types = type -> true;
        private Documentation documentation = Documentation.NONE;
        private final Map<Class<?>, String> groups = new LinkedHashMap<>();

        private Builder() {}

        /**
         * Whether deprecated methods are skipped. Defaults to true.
         */
        public Builder excludeDeprecated(boolean excludeDeprecated) {
            this.excludeDeprecated = excludeDeprecated;
            return this;
        }

        /**
         * Skips methods with these names on every class.
         */
        public Builder excludeMethods(String... names) {
            excludedMethods.addAll(List.of(names));
            return this;
        }

        /**
         * Skips methods declared by these classes, e.g. a common base class whose members are plumbing.
         */
        public Builder excludeDeclaredBy(Class<?>... classes) {
            excludedDeclarers.addAll(List.of(classes));
            return this;
        }

        /**
         * Only methods whose port types pass this filter become ports. Combines with earlier filters.
         */
        public Builder acceptTypes(Predicate<PortType> filter) {
            types = types.and(filter);
            return this;
        }

        /**
         * Sets where derived nodes take their descriptions from. Defaults to none.
         */
        public Builder documentation(Documentation documentation) {
            this.documentation = documentation;
            return this;
        }

        /**
         * Groups the properties of large nodes by the type that brings them, e.g. "Living Entity" for the health of a
         * creeper, given every level of the hierarchy that should form a group.
         *
         * @param type a level of a type hierarchy
         * @param name its editor name
         * @return this builder
         */
        public Builder group(Class<?> type, String name) {
            groups.put(type, name);
            return this;
        }

        public ReflectionRules build() {
            return new ReflectionRules(
                    excludeDeprecated, excludedMethods, excludedDeclarers, types, documentation, groups);
        }
    }
}
