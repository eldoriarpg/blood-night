/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.types;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.PortType;
import tools.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Consumer-provided knowledge about types: names for the editor and literal codecs for config values.
 * <p>
 * Primitive, wrapper and string codecs are built in; enums get a codec automatically. Other types used in config
 * fields need a registered codec.
 */
public final class TypeMapping {
    private static final Map<Class<?>, Class<?>> WRAPPERS = Map.of(
            Boolean.class,
            boolean.class,
            Integer.class,
            int.class,
            Long.class,
            long.class,
            Double.class,
            double.class,
            Float.class,
            float.class);

    private final Map<Class<?>, String> names;
    private final Map<Class<?>, LiteralCodec<?>> codecs;
    private final Map<String, PortType> selectable;

    private TypeMapping(
            Map<Class<?>, String> names, Map<Class<?>, LiteralCodec<?>> codecs, Map<String, PortType> selectable) {
        this.names = Map.copyOf(names);
        this.codecs = Map.copyOf(codecs);
        this.selectable = Collections.unmodifiableMap(new LinkedHashMap<>(selectable));
    }

    /**
     * @return a builder holding the built-in codecs
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return the built-in mapping
     */
    public static TypeMapping defaults() {
        return builder().build();
    }

    /**
     * @param type a port type
     * @return the registered editor name, or the simple class name with type arguments
     */
    public String name(PortType type) {
        return switch (type) {
            case PortType.Variable variable -> variable.name();
            case PortType.Concrete concrete -> {
                String base = names.getOrDefault(concrete.raw(), concrete.raw().getSimpleName());
                if (concrete.arguments().isEmpty()) yield base;
                yield base
                        + concrete.arguments().stream()
                                .map(this::name)
                                .toList()
                                .toString()
                                .replace('[', '<')
                                .replace(']', '>');
            }
        };
    }

    /**
     * @param type a port type
     * @return the codec for config values of the type, if it has one; generic types never do
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<LiteralCodec<?>> codec(PortType type) {
        if (!(type instanceof PortType.Concrete concrete)
                || !concrete.arguments().isEmpty()) return Optional.empty();
        Class<?> raw = WRAPPERS.getOrDefault(concrete.raw(), concrete.raw());
        LiteralCodec<?> codec = codecs.get(raw);
        if (codec == null && raw.isEnum()) codec = Codecs.forEnum((Class) raw);
        return Optional.ofNullable(codec);
    }

    /**
     * Reads a config value and emits it as a literal.
     *
     * @param type  declared type of the config field
     * @param value value from the graph
     * @return the literal expression
     * @throws IllegalArgumentException when the type has no codec or the value is invalid
     */
    public CodeBlock literal(PortType type, JsonNode value) {
        LiteralCodec<?> codec =
                codec(type).orElseThrow(() -> new IllegalArgumentException("No literal codec for " + type.id()));
        return codec.literal(value);
    }

    /**
     * @param type a port type
     * @return the values its codec offers, e.g. enum constants or registry keys, for editor dropdowns
     */
    public List<String> enumValues(PortType type) {
        return codec(type).map(LiteralCodec::values).orElse(List.of());
    }

    /**
     * Types users can pick themselves, e.g. for a macro input, by id. Defaults: {@code boolean}, {@code integer},
     * {@code number}, {@code text}.
     *
     * @return selectable types by id, in registration order
     */
    public Map<String, PortType> selectable() {
        return selectable;
    }

    /**
     * Builder for {@link TypeMapping}.
     */
    public static final class Builder {
        private final Map<Class<?>, String> names = new HashMap<>();
        private final Map<Class<?>, LiteralCodec<?>> codecs = new HashMap<>();
        private final Map<String, PortType> selectable = new LinkedHashMap<>();

        private Builder() {
            Codecs.DEFAULTS.forEach(this::codec);
            name(boolean.class, "Boolean")
                    .name(int.class, "Integer")
                    .name(long.class, "Long")
                    .name(double.class, "Number")
                    .name(float.class, "Float")
                    .name(String.class, "Text");
            selectable("boolean", PortType.of(boolean.class))
                    .selectable("integer", PortType.of(int.class))
                    .selectable("number", PortType.of(double.class))
                    .selectable("text", PortType.of(String.class));
        }

        /**
         * Lets users pick a type by id, e.g. for macro inputs. The id must be a lower-case segment.
         */
        public Builder selectable(String id, PortType type) {
            if (!id.matches("[a-z][a-z0-9_]*")) throw new IllegalArgumentException("Invalid type id " + id);
            if (type.hasVariables())
                throw new IllegalArgumentException("Selectable types must be concrete: " + type.id());
            selectable.put(id, type);
            return this;
        }

        /**
         * Sets the name the editor shows for a type.
         */
        public Builder name(Class<?> type, String name) {
            names.put(type, name);
            return this;
        }

        /**
         * Registers a codec, replacing any codec for the same type.
         */
        public Builder codec(LiteralCodec<?> codec) {
            codecs.put(codec.type(), codec);
            return this;
        }

        public TypeMapping build() {
            return new TypeMapping(names, codecs, selectable);
        }
    }
}
