package de.eldoria.graph.reflect;

import de.eldoria.graph.definition.PortType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

    private ReflectionRules(boolean excludeDeprecated, Set<String> excludedMethods, Set<Class<?>> excludedDeclarers, Predicate<PortType> types) {
        this.excludeDeprecated = excludeDeprecated;
        this.excludedMethods = Set.copyOf(excludedMethods);
        this.excludedDeclarers = Set.copyOf(excludedDeclarers);
        this.types = types;
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

        private Builder() {
        }

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

        public ReflectionRules build() {
            return new ReflectionRules(excludeDeprecated, excludedMethods, excludedDeclarers, types);
        }
    }
}
