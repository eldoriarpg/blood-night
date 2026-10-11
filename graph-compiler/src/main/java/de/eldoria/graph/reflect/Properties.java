package de.eldoria.graph.reflect;

import de.eldoria.graph.definition.PortType;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
    private static final Set<String> KEYWORDS = Set.of("abstract", "assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally",
            "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
            "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
            "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false",
            "null", "var", "yield", "record", "sealed", "permits");

    private Properties() {
    }

    /**
     * A readable property.
     *
     * @param id     snake_case id derived from the method name
     * @param method the getter
     * @param type   its return type
     */
    record Getter(String id, Method method, PortType type) {
    }

    /**
     * A writable property.
     *
     * @param id       node id suffix, {@code set_<property>}; overloads get the parameter type appended
     * @param property snake_case property id, shared by overloads
     * @param method   the setter
     * @param type     its parameter type
     */
    record Setter(String id, String property, Method method, PortType type) {
    }

    /**
     * Public getters: {@code getX()}, {@code isX()} returning boolean, and record component accessors. Where a class
     * has both {@code getX} and {@code isX}, the getter wins. Covariant overrides collapse to the most specific
     * return type.
     *
     * @return getters ordered by id
     */
    static List<Getter> getters(Class<?> type, ReflectionRules rules) {
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
        return List.copyOf(byId.values());
    }

    /**
     * Public single-argument {@code setX} methods. Their return value is ignored, so fluent setters work too. When
     * several overloads take one argument, the first by parameter type name keeps the plain id and the others get
     * the parameter type appended.
     *
     * @return setters ordered by id
     */
    static List<Setter> setters(Class<?> type, ReflectionRules rules) {
        Map<String, List<Method>> byProperty = new TreeMap<>();
        for (Method method : type.getMethods()) {
            if (method.getParameterCount() != 1 || !isAccessor(method.getName(), "set")) continue;
            if (!rules.accepts(method)) continue;
            byProperty.computeIfAbsent(snakeCase(method.getName().substring(3)), key -> new ArrayList<>()).add(method);
        }
        Map<String, Setter> byId = new TreeMap<>();
        byProperty.forEach((property, methods) -> {
            methods.sort(Comparator.comparing(method -> method.getParameterTypes()[0].getName()));
            for (Method method : methods) {
                Optional<PortType> portType = portType(method.getGenericParameterTypes()[0], rules);
                if (portType.isEmpty()) continue;
                String id = byId.containsKey("set_" + property)
                        ? "set_%s_%s".formatted(property, snakeCase(method.getParameterTypes()[0].getSimpleName()))
                        : "set_" + property;
                byId.putIfAbsent(id, new Setter(id, property, method, portType.get()));
            }
        });
        return List.copyOf(byId.values());
    }

    /**
     * Turns a Java name into a port id: {@code FinalDamage} → {@code final_damage}, {@code UUIDString} →
     * {@code uuid_string}. Java keywords get a trailing underscore, so ids double as variable names.
     */
    static String snakeCase(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char current = name.charAt(i);
            if (Character.isUpperCase(current) && i > 0) {
                char previous = name.charAt(i - 1);
                boolean nextIsLower = i + 1 < name.length() && Character.isLowerCase(name.charAt(i + 1));
                if (Character.isLowerCase(previous) || Character.isDigit(previous) || (Character.isUpperCase(previous) && nextIsLower)) {
                    result.append('_');
                }
            }
            result.append(Character.toLowerCase(current));
        }
        String id = result.toString().replaceAll("[^a-z0-9_]", "").replaceAll("^[^a-z]+", "");
        if (id.isEmpty()) throw new IllegalArgumentException("Cannot derive an id from " + name);
        return KEYWORDS.contains(id) ? id + "_" : id;
    }

    private static Optional<String> propertyOf(Method method, Class<?> owner) {
        String name = method.getName();
        if (isAccessor(name, "get")) return Optional.of(snakeCase(name.substring(3)));
        if (isAccessor(name, "is") && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            return Optional.of(snakeCase(name.substring(2)));
        }
        if (owner.isRecord()) {
            for (RecordComponent component : owner.getRecordComponents()) {
                if (component.getAccessor().getName().equals(name)) return Optional.of(snakeCase(name));
            }
        }
        return Optional.empty();
    }

    private static boolean isAccessor(String name, String prefix) {
        return name.length() > prefix.length() && name.startsWith(prefix) && Character.isUpperCase(name.charAt(prefix.length()));
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
