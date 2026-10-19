/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Writes JSON Schemas (draft 2020-12, also OpenAPI 3.1) of the classes a mapper with field visibility reads and
 * writes: records by their components, other classes by their fields, as the configuration and the editor payload
 * are mapped.
 * <p>
 * Every class of the {@code de.eldoria} packages and every enum becomes a definition, referenced by its simple name
 * (qualified by its package or outer class when two share one). Enums list their constants; collections become arrays (sets with unique items); maps become objects whose values follow the
 * value type; {@link JsonNode} and {@link Object} accept anything; {@link Instant} is a date-time string. Classes of
 * other libraries that are not enums, such as item stacks, are opaque objects. Record components are required unless
 * nullable or their record or the component itself leaves values out ({@link JsonInclude} other than always); fields of classes are optional and carry the value a new instance has as their default. Field names
 * follow {@link JsonProperty}; {@link JsonIgnore}d, static and transient fields are left out.
 */
public final class JsonSchemas {
    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;
    private final String referencePrefix;
    private final ObjectMapper mapper;
    private final Map<String, ObjectNode> definitions = new TreeMap<>();
    private final Map<String, Class<?>> owners = new TreeMap<>();

    /**
     * @param referencePrefix where definitions are referenced from, {@code #/$defs/} or {@code #/components/schemas/}
     * @param mapper          writes the defaults of class fields as the configuration would
     */
    public JsonSchemas(String referencePrefix, ObjectMapper mapper) {
        this.referencePrefix = referencePrefix;
        this.mapper = mapper;
    }

    /**
     * @param root the class the document describes
     * @return a schema document: the root's schema with every definition it needs under {@code $defs}
     */
    public static ObjectNode document(Class<?> root, ObjectMapper mapper) {
        JsonSchemas schemas = new JsonSchemas("#/$defs/", mapper);
        ObjectNode document = NODES.objectNode();
        document.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        document.put("title", root.getSimpleName());
        document.setAll(schemas.schema(root));
        document.set("$defs", schemas.definitions());
        return document;
    }

    /**
     * @param type a type to describe; its definitions are collected
     * @return its schema, a reference for classes with a definition
     */
    public ObjectNode schema(Type type) {
        return schema(type, false);
    }

    /**
     * @return every definition collected so far, by name
     */
    public ObjectNode definitions() {
        ObjectNode all = NODES.objectNode();
        definitions.forEach(all::set);
        return all;
    }

    private ObjectNode schema(Type type, boolean unique) {
        if (type instanceof ParameterizedType parameterized) return parameterized(parameterized);
        if (type instanceof GenericArrayType array) return array(schema(array.getGenericComponentType()), false);
        if (type instanceof WildcardType wildcard) return schema(wildcard.getUpperBounds()[0]);
        if (!(type instanceof Class<?> clazz)) return NODES.objectNode();
        if (clazz.isArray()) return array(schema(clazz.getComponentType()), false);
        ObjectNode simple = simple(clazz);
        if (simple != null) return simple;
        if (clazz.isEnum()) return reference(clazz);
        if (Collection.class.isAssignableFrom(clazz))
            return array(NODES.objectNode(), Set.class.isAssignableFrom(clazz) || unique);
        if (Map.class.isAssignableFrom(clazz)) return NODES.objectNode().put("type", "object");
        if (!clazz.getName().startsWith("de.eldoria."))
            return NODES.objectNode().put("type", "object");
        return reference(clazz);
    }

    private ObjectNode parameterized(ParameterizedType type) {
        Class<?> raw = (Class<?>) type.getRawType();
        Type[] arguments = type.getActualTypeArguments();
        if (Collection.class.isAssignableFrom(raw)) {
            return array(schema(arguments[0]), Set.class.isAssignableFrom(raw));
        }
        if (Map.class.isAssignableFrom(raw)) {
            ObjectNode map = NODES.objectNode().put("type", "object");
            map.set("additionalProperties", schema(arguments[1]));
            return map;
        }
        return schema(raw);
    }

    private static ObjectNode array(ObjectNode items, boolean unique) {
        ObjectNode array = NODES.objectNode().put("type", "array");
        array.set("items", items);
        if (unique) array.put("uniqueItems", true);
        return array;
    }

    private static @Nullable ObjectNode simple(Class<?> clazz) {
        if (clazz == String.class || clazz == UUID.class || clazz == Character.class || clazz == char.class) {
            return NODES.objectNode().put("type", "string");
        }
        if (clazz == boolean.class || clazz == Boolean.class)
            return NODES.objectNode().put("type", "boolean");
        if (clazz == int.class
                || clazz == Integer.class
                || clazz == long.class
                || clazz == Long.class
                || clazz == short.class
                || clazz == Short.class
                || clazz == byte.class
                || clazz == Byte.class) {
            return NODES.objectNode().put("type", "integer");
        }
        if (clazz == double.class
                || clazz == Double.class
                || clazz == float.class
                || clazz == Float.class
                || Number.class.isAssignableFrom(clazz)) {
            return NODES.objectNode().put("type", "number");
        }
        if (clazz == Instant.class)
            return NODES.objectNode().put("type", "string").put("format", "date-time");
        if (clazz == Object.class || JsonNode.class.isAssignableFrom(clazz)) return NODES.objectNode();
        return null;
    }

    private static ObjectNode enumeration(Class<?> clazz) {
        ObjectNode schema = NODES.objectNode().put("type", "string");
        ArrayNode values = schema.putArray("enum");
        for (Object constant : clazz.getEnumConstants()) values.add(((Enum<?>) constant).name());
        return schema;
    }

    private ObjectNode reference(Class<?> clazz) {
        String name = name(clazz);
        if (!definitions.containsKey(name)) {
            definitions.put(name, NODES.objectNode());
            definitions.put(
                    name, clazz.isEnum() ? enumeration(clazz) : clazz.isRecord() ? record(clazz) : fields(clazz));
        }
        return NODES.objectNode().put("$ref", referencePrefix + name);
    }

    private String name(Class<?> clazz) {
        String name = clazz.getSimpleName();
        Class<?> owner = owners.putIfAbsent(name, clazz);
        if (owner == null || owner == clazz) return name;
        String qualified = clazz.getName()
                .substring(clazz.getName().lastIndexOf('.', clazz.getName().lastIndexOf('.') - 1) + 1)
                .replace('.', '_')
                .replace('$', '_');
        owners.putIfAbsent(qualified, clazz);
        return qualified;
    }

    private ObjectNode record(Class<?> clazz) {
        ObjectNode schema = NODES.objectNode().put("type", "object");
        ObjectNode properties = schema.putObject("properties");
        List<String> required = new ArrayList<>();
        boolean omitsEmpty = omitsValues(clazz);
        for (RecordComponent component : clazz.getRecordComponents()) {
            if (ignored(component)) continue;
            String property = propertyName(component, component.getName());
            properties.set(property, schema(component.getGenericType()));
            boolean omitted = omitsEmpty || omits(component.getAccessor().getAnnotation(JsonInclude.class));
            if (!omitted && !nullable(component, component.getAnnotatedType())) required.add(property);
        }
        if (!required.isEmpty()) required.forEach(schema.putArray("required")::add);
        return schema;
    }

    private ObjectNode fields(Class<?> clazz) {
        ObjectNode schema = NODES.objectNode().put("type", "object");
        ObjectNode properties = schema.putObject("properties");
        Object instance = instantiate(clazz);
        for (Field field : fields(clazz, new ArrayList<>())) {
            if (ignored(field)) continue;
            ObjectNode property = schema(field.getGenericType());
            MaxItems maxItems = field.getAnnotation(MaxItems.class);
            if (maxItems != null) property.put("maxItems", maxItems.value());
            JsonNode value = defaultValue(field, instance);
            if (value != null && !value.isNull()) {
                if (property.has("$ref")) {
                    ObjectNode wrapped = NODES.objectNode();
                    wrapped.putArray("allOf").add(property);
                    property = wrapped;
                }
                property.set("default", value);
            }
            properties.set(propertyName(field, field.getName()), property);
        }
        return schema;
    }

    private static List<Field> fields(Class<?> clazz, List<Field> collected) {
        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class)
            fields(clazz.getSuperclass(), collected);
        collected.addAll(Arrays.asList(clazz.getDeclaredFields()));
        return collected;
    }

    private static boolean omitsValues(Class<?> clazz) {
        return omits(clazz.getAnnotation(JsonInclude.class));
    }

    private static boolean omits(@Nullable JsonInclude include) {
        return include != null
                && include.value() != JsonInclude.Include.ALWAYS
                && include.value() != JsonInclude.Include.USE_DEFAULTS;
    }

    private static boolean ignored(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers)
                || Modifier.isTransient(modifiers)
                || field.isSynthetic()
                || field.isAnnotationPresent(JsonIgnore.class);
    }

    private static boolean ignored(RecordComponent component) {
        return component.isAnnotationPresent(JsonIgnore.class);
    }

    private static String propertyName(AnnotatedElement element, String fallback) {
        JsonProperty property = element.getAnnotation(JsonProperty.class);
        return property == null || property.value().isEmpty() ? fallback : property.value();
    }

    private static boolean nullable(AnnotatedElement element, AnnotatedType type) {
        return Arrays.stream(element.getAnnotations())
                        .anyMatch(a -> a.annotationType().getSimpleName().equals("Nullable"))
                || Arrays.stream(type.getAnnotations())
                        .anyMatch(a -> a.annotationType().getSimpleName().equals("Nullable"));
    }

    private static @Nullable Object instantiate(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    private @Nullable JsonNode defaultValue(Field field, @Nullable Object instance) {
        if (instance == null) return null;
        try {
            field.setAccessible(true);
            Object value = field.get(instance);
            return value == null ? null : mapper.valueToTree(value);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }
}
