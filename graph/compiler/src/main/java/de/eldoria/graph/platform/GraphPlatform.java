/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.platform;

import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.entry.EntryParameter;
import de.eldoria.graph.definition.port.ConfigField;
import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.types.TypeMapping;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Everything a consumer provides to turn graphs into code: the host interface generated classes implement, an
 * optional context object passed to their constructor, the node definitions, the type mapping and the compile
 * classpath.
 * <p>
 * {@link Builder#build()} checks the whole set at once and reports every problem together, so a consumer sees all
 * broken definitions in one run.
 */
public final class GraphPlatform {
    private final Class<?> host;
    private final Class<?> context;
    private final TypeMapping types;
    private final Map<String, NodeDefinition> nodes;
    private final List<Path> classpath;
    private final Map<String, List<NodeRule>> rules;
    private final Map<String, Binding> bindings;
    private final @Nullable Method clock;

    private GraphPlatform(
            Class<?> host,
            Class<?> context,
            TypeMapping types,
            Map<String, NodeDefinition> nodes,
            List<Path> classpath,
            Map<String, List<NodeRule>> rules,
            Map<String, Binding> bindings,
            @Nullable Method clock) {
        this.host = host;
        this.clock = clock;
        this.bindings = Map.copyOf(bindings);
        this.context = context;
        this.types = types;
        this.nodes = Map.copyOf(nodes);
        this.classpath = List.copyOf(classpath);
        Map<String, List<NodeRule>> copy = new LinkedHashMap<>();
        rules.forEach((type, list) -> copy.put(type, List.copyOf(list)));
        this.rules = Map.copyOf(copy);
    }

    /**
     * Starts a platform.
     *
     * @param host interface generated classes implement; entry points are its methods
     * @return a builder
     */
    public static Builder builder(Class<?> host) {
        return new Builder(host);
    }

    /**
     * @return interface generated classes implement
     */
    public Class<?> host() {
        return host;
    }

    /**
     * @return type of the object passed to the constructor of generated classes, if any
     */
    public Optional<Class<?>> context() {
        return Optional.ofNullable(context);
    }

    /**
     * @return editor names and literal codecs
     */
    public TypeMapping types() {
        return types;
    }

    /**
     * @param id stable node id
     * @return the definition, if registered
     */
    public Optional<NodeDefinition> node(String id) {
        return Optional.ofNullable(nodes.get(id));
    }

    /**
     * @return all definitions by id
     */
    public Map<String, NodeDefinition> nodes() {
        return nodes;
    }

    /**
     * @return jars and directories generated code compiles against
     */
    public List<Path> classpath() {
        return classpath;
    }

    /**
     * @param type a node type id
     * @return the platform's own checks for nodes of that type
     */
    public List<NodeRule> rules(String type) {
        return rules.getOrDefault(type, List.of());
    }

    /**
     * @return the graph-wide type variables by name; see {@link Binding}
     */
    public Map<String, Binding> bindings() {
        return bindings;
    }

    /**
     * @return the host method the host calls once per tick, which code that has to run every tick, such as pending
     *     delays, is added to; empty when the platform has no clock
     */
    public Optional<Method> clock() {
        return Optional.ofNullable(clock);
    }

    /**
     * Builder for {@link GraphPlatform}.
     */
    public static final class Builder {
        private final Class<?> host;
        private final Map<String, NodeDefinition> nodes = new LinkedHashMap<>();
        private final List<String> problems = new ArrayList<>();
        private final List<Path> classpath = new ArrayList<>();
        private final Map<String, List<NodeRule>> rules = new LinkedHashMap<>();
        private final Map<String, Binding> bindings = new LinkedHashMap<>();
        private Class<?> context;
        private TypeMapping types = TypeMapping.defaults();
        private @Nullable String clock;

        private Builder(Class<?> host) {
            this.host = Objects.requireNonNull(host, "host");
        }

        /**
         * Sets the type of the object every generated class receives in its constructor.
         */
        public Builder context(Class<?> context) {
            this.context = context;
            return this;
        }

        /**
         * Names the host method the host calls once per tick, without parameters and returning nothing. Nodes that wait,
         * such as a delay, need one.
         *
         * @param method the method's name
         * @return this builder
         */
        public Builder clock(String method) {
            this.clock = method;
            return this;
        }

        public Builder types(TypeMapping types) {
            this.types = Objects.requireNonNull(types, "types");
            return this;
        }

        /**
         * Registers node definitions. Ids must be unique.
         */
        public Builder define(NodeDefinition... definitions) {
            return define(Arrays.asList(definitions));
        }

        /**
         * Registers node definitions. Ids must be unique.
         */
        public Builder define(Collection<? extends NodeDefinition> definitions) {
            for (NodeDefinition definition : definitions) {
                if (nodes.putIfAbsent(definition.id(), definition) != null)
                    problems.add("Duplicate node id " + definition.id());
            }
            return this;
        }

        public Builder classpath(Collection<Path> paths) {
            classpath.addAll(paths);
            return this;
        }

        /**
         * Adds a check run on every node of a type when a graph is validated.
         *
         * @param type node type id
         * @param rule the check
         * @return this builder
         */
        public Builder rule(String type, NodeRule rule) {
            rules.computeIfAbsent(type, key -> new ArrayList<>()).add(rule);
            return this;
        }

        /**
         * Declares a graph-wide type variable; see {@link Binding}.
         *
         * @param variable the variable's name, as node definitions use it
         * @param binding  its fallback and candidates
         * @return this builder
         */
        public Builder binding(String variable, Binding binding) {
            bindings.put(Objects.requireNonNull(variable, "variable"), Objects.requireNonNull(binding, "binding"));
            return this;
        }

        /**
         * Declares the graph-wide type variables of another platform, e.g. when building one from it.
         *
         * @param bindings the variables by name
         * @return this builder
         */
        public Builder bindings(Map<String, Binding> bindings) {
            this.bindings.putAll(bindings);
            return this;
        }

        /**
         * @return the platform
         * @throws PlatformException listing every problem found
         */
        public GraphPlatform build() {
            List<String> found = new ArrayList<>(problems);
            if (!host.isInterface()) found.add("Host %s is not an interface".formatted(host.getName()));
            Map<Method, String> entryMethods = new LinkedHashMap<>();
            for (NodeDefinition definition : nodes.values()) {
                definition.descriptor().config().forEach(field -> checkConfig(definition, field, found));
                if (definition instanceof EntryDefinition entry) {
                    checkEntry(entry, found).ifPresent(method -> {
                        String previous = entryMethods.putIfAbsent(method, entry.id());
                        if (previous != null)
                            found.add("%s and %s both implement %s".formatted(previous, entry.id(), method.getName()));
                    });
                }
            }
            if (!found.isEmpty()) throw new PlatformException(found);
            for (String type : rules.keySet()) {
                if (!nodes.containsKey(type)) found.add("Rule for unknown node type " + type);
            }
            Method tick = clock == null ? null : clockMethod(found);
            if (!found.isEmpty()) throw new PlatformException(found);
            return new GraphPlatform(host, context, types, nodes, classpath, rules, bindings, tick);
        }

        private @Nullable Method clockMethod(List<String> found) {
            try {
                Method method = host.getMethod(Objects.requireNonNull(clock));
                if (method.getReturnType() == void.class) return method;
                found.add("Clock %s must return nothing".formatted(clock));
            } catch (NoSuchMethodException e) {
                found.add("Host %s has no clock method %s()".formatted(host.getName(), clock));
            }
            return null;
        }

        private void checkConfig(NodeDefinition definition, ConfigField field, List<String> found) {
            if (field.type() instanceof PortType.Variable variable) {
                boolean picked = definition.descriptor().config().stream()
                        .anyMatch(other -> variable.name().equals(other.selects()));
                if (!picked)
                    found.add("%s: config field '%s' has type %s, which no type field picks"
                            .formatted(definition.id(), field.id(), variable.name()));
                return;
            }
            for (String choice : field.choices()) {
                if (!types.selectable().containsKey(choice))
                    found.add("%s: config field '%s' offers %s, which is no selectable type"
                            .formatted(definition.id(), field.id(), choice));
            }
            var codec = types.codec(field.type());
            if (codec.isEmpty()) {
                found.add("%s: config field '%s' has type %s without a literal codec"
                        .formatted(definition.id(), field.id(), field.type().id()));
                return;
            }
            field.defaultIfAbsent().ifPresent(value -> {
                try {
                    codec.get().read(value);
                } catch (IllegalArgumentException e) {
                    found.add("%s: default of config field '%s' is invalid: %s"
                            .formatted(definition.id(), field.id(), e.getMessage()));
                }
            });
        }

        private Optional<Method> checkEntry(EntryDefinition entry, List<String> found) {
            Class<?>[] parameterTypes = entry.parameters().stream()
                    .map(EntryParameter::type)
                    .map(Builder::rawClass)
                    .toArray(Class<?>[]::new);
            if (Arrays.asList(parameterTypes).contains(null)) {
                found.add("%s: entry parameters must have concrete types".formatted(entry.id()));
                return Optional.empty();
            }
            try {
                Method method = host.getMethod(entry.method(), parameterTypes);
                if (method.getReturnType() != void.class)
                    found.add("%s: host method %s must return void".formatted(entry.id(), entry.method()));
                if (Modifier.isStatic(method.getModifiers()))
                    found.add("%s: host method %s must not be static".formatted(entry.id(), entry.method()));
                return Optional.of(method);
            } catch (NoSuchMethodException e) {
                found.add("%s: host %s has no method %s(%s)"
                        .formatted(
                                entry.id(),
                                host.getSimpleName(),
                                entry.method(),
                                String.join(
                                        ", ",
                                        Arrays.stream(parameterTypes)
                                                .map(Class::getSimpleName)
                                                .toList())));
                return Optional.empty();
            }
        }

        private static @Nullable Class<?> rawClass(PortType type) {
            return type instanceof PortType.Concrete concrete ? concrete.raw() : null;
        }
    }
}
