/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.reflect;

import de.eldoria.graph.definition.PortType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Finds the readable and writable properties of a class.
 */
final class Properties {
    private static final List<Class<?>> WIDEST_FIRST = List.of(
            double.class, Double.class,
            float.class, Float.class,
            long.class, Long.class,
            int.class, Integer.class,
            short.class, Short.class,
            byte.class, Byte.class);

    private static final Set<String> OBJECT_METHODS =
            Set.of("toString", "hashCode", "getClass", "clone", "notify", "notifyAll", "wait", "finalize");

    private Properties() {}

    /**
     * Which methods besides {@code getX}, {@code isX} and {@code setX} count as accessors.
     *
     * @param fluent whether {@code x()} counts as a getter when backed by a field {@code x} of the same type or paired
     *               with a setter {@code x(T)}, and such a setter then counts too
     * @param manual names of {@code x()} methods that count as getters regardless, with their {@code x(T)} setters
     */
    record Style(boolean fluent, Set<String> manual) {
        static final Style STANDARD = new Style(false, Set.of());

        Style {
            manual = Set.copyOf(manual);
        }

        boolean active() {
            return fluent || !manual.isEmpty();
        }
    }

    /**
     * A readable property.
     *
     * @param id     snake_case id derived from the method name
     * @param method the getter
     * @param type   its return type
     */
    record Getter(String id, Method method, PortType type) {}

    /**
     * A writable property.
     *
     * @param id       node id suffix, {@code set_<property>}; overloads get the parameter type appended
     * @param property snake_case property id, shared by overloads
     * @param method   the setter
     * @param type     its parameter type
     */
    record Setter(String id, String property, Method method, PortType type) {}

    /**
     * Public getters: {@code getX()}, {@code isX()} returning boolean, and record component accessors. Where a class
     * has both {@code getX} and {@code isX}, the getter wins. Covariant overrides collapse to the most specific
     * return type.
     *
     * @return getters ordered by id
     */
    static List<Getter> getters(Class<?> type, ReflectionRules rules) {
        return getters(type, rules, Style.STANDARD);
    }

    /**
     * Like {@link #getters(Class, ReflectionRules)}, plus record-style {@code x()} getters as the style allows. A
     * get-style getter wins over a record-style one with the same id.
     *
     * @return getters ordered by id
     */
    static List<Getter> getters(Class<?> type, ReflectionRules rules, Style style) {
        Map<String, Method> byName = new LinkedHashMap<>();
        for (Method method : type.getMethods()) {
            if (method.getParameterCount() != 0 || method.getReturnType() == void.class) continue;
            if (propertyOf(method, type).isEmpty()) continue;
            byName.merge(method.getName(), method, Properties::moreSpecific);
        }
        Map<String, Getter> byId = new TreeMap<>();
        byName.values().stream()
                .sorted(Comparator.comparing(method -> method.getName().startsWith("is") ? 1 : 0))
                .forEach(method -> {
                    if (!rules.accepts(method)) return;
                    String id = propertyOf(method, type).orElseThrow();
                    portType(method.getGenericReturnType(), rules)
                            .ifPresent(portType -> byId.putIfAbsent(id, new Getter(id, method, portType)));
                });
        if (style.active()) {
            fluentGetters(type, style).values().forEach(method -> {
                if (!rules.accepts(method)) return;
                String id = Names.snakeCase(method.getName());
                portType(method.getGenericReturnType(), rules)
                        .ifPresent(portType -> byId.putIfAbsent(id, new Getter(id, method, portType)));
            });
        }
        return List.copyOf(byId.values());
    }

    private static Map<String, Method> fluentGetters(Class<?> type, Style style) {
        Map<String, Method> byName = new TreeMap<>();
        for (Method method : type.getMethods()) {
            String name = method.getName();
            if (method.getParameterCount() != 0 || method.getReturnType() == void.class) continue;
            if (OBJECT_METHODS.contains(name) || propertyOf(method, type).isPresent()) continue;
            boolean qualifies = style.manual().contains(name)
                    || style.fluent()
                            && (hasField(type, name, method.getReturnType())
                                    || fluentSetter(type, name, method.getReturnType())
                                            .isPresent());
            if (qualifies) byName.merge(name, method, Properties::moreSpecific);
        }
        return byName;
    }

    private static boolean hasField(Class<?> type, String name, Class<?> fieldType) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.getName().equals(name) && field.getType() == fieldType) return true;
            }
        }
        return false;
    }

    private static Optional<Method> fluentSetter(Class<?> type, String name, Class<?> valueType) {
        return Arrays.stream(type.getMethods())
                .filter(method -> method.getName().equals(name)
                        && method.getParameterCount() == 1
                        && method.getParameterTypes()[0] == valueType)
                .findFirst();
    }

    /**
     * Public single-argument {@code setX} methods. Their return value is ignored, so fluent setters work too. When
     * several overloads take one argument, the widest number type comes first (double, float, long, int, short, byte),
     * then the others by parameter type name; the first keeps the plain id and the others get the parameter type
     * appended.
     *
     * @return setters ordered by id
     */
    static List<Setter> setters(Class<?> type, ReflectionRules rules) {
        return setters(type, rules, Style.STANDARD);
    }

    /**
     * Like {@link #setters(Class, ReflectionRules)}, plus a record-style {@code x(T)} setter for every record-style
     * getter {@code x()} the style accepts, when {@code T} is the getter's return type.
     *
     * @return setters ordered by id
     */
    static List<Setter> setters(Class<?> type, ReflectionRules rules, Style style) {
        Map<String, List<Method>> byProperty = new TreeMap<>();
        for (Method method : type.getMethods()) {
            if (method.getParameterCount() != 1 || !isAccessor(method.getName(), "set")) continue;
            if (!rules.accepts(method)) continue;
            byProperty
                    .computeIfAbsent(Names.snakeCase(method.getName().substring(3)), key -> new ArrayList<>())
                    .add(method);
        }
        Map<String, Setter> byId = new TreeMap<>();
        byProperty.forEach((property, methods) -> {
            methods.sort(Comparator.comparing((Method method) -> narrowness(method.getParameterTypes()[0]))
                    .thenComparing(method -> method.getParameterTypes()[0].getName()));
            for (Method method : methods) {
                Optional<PortType> portType = portType(method.getGenericParameterTypes()[0], rules);
                if (portType.isEmpty()) continue;
                String id = byId.containsKey("set_" + property)
                        ? "set_%s_%s"
                                .formatted(property, Names.snakeCase(method.getParameterTypes()[0].getSimpleName()))
                        : "set_" + property;
                byId.putIfAbsent(id, new Setter(id, property, method, portType.get()));
            }
        });
        if (style.active()) {
            fluentGetters(type, style).values().forEach(getter -> fluentSetter(
                            type, getter.getName(), getter.getReturnType())
                    .filter(rules::accepts)
                    .ifPresent(setter -> {
                        String property = Names.snakeCase(getter.getName());
                        portType(setter.getGenericParameterTypes()[0], rules)
                                .ifPresent(portType -> byId.putIfAbsent(
                                        "set_" + property, new Setter("set_" + property, property, setter, portType)));
                    }));
        }
        return List.copyOf(byId.values());
    }

    private static int narrowness(Class<?> type) {
        int index = WIDEST_FIRST.indexOf(type);
        return index < 0 ? WIDEST_FIRST.size() : index / 2;
    }

    private static Optional<String> propertyOf(Method method, Class<?> owner) {
        String name = method.getName();
        if (isAccessor(name, "get")) return Optional.of(Names.snakeCase(name.substring(3)));
        if (isAccessor(name, "is")
                && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            return Optional.of(Names.snakeCase(name.substring(2)));
        }
        if (owner.isRecord()) {
            for (RecordComponent component : owner.getRecordComponents()) {
                if (component.getAccessor().getName().equals(name)) return Optional.of(Names.snakeCase(name));
            }
        }
        return Optional.empty();
    }

    private static boolean isAccessor(String name, String prefix) {
        return name.length() > prefix.length()
                && name.startsWith(prefix)
                && Character.isUpperCase(name.charAt(prefix.length()));
    }

    private static Method moreSpecific(Method a, Method b) {
        return a.getReturnType().isAssignableFrom(b.getReturnType()) ? b : a;
    }

    private static Optional<PortType> portType(Type type, ReflectionRules rules) {
        try {
            PortType portType = PortType.of(type);
            return rules.accepts(portType) ? Optional.of(portType) : Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
