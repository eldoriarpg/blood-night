/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.reflect;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.definition.StatementDefinition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Derives nodes from the public getters and setters of a class.
 * <p>
 * For a class registered under prefix {@code location}:
 * <ul>
 *     <li>{@code location.read}: a pure node taking the object as {@code target}, one output per getter;</li>
 *     <li>{@code location.mutate}: an action taking {@code target} and every setter as an optional input, applying
 *     those that are given.</li>
 * </ul>
 * Setters act on whatever object is connected to {@code target}. When that object is an entry's output, such as an
 * event, the scope rules of the compiler already restrict the setter to that entry's chain.
 * <p>
 * Both nodes are variants of the {@link #MUTATE} node the editor shows in their place: it reads while no property is
 * set and changes them as soon as one is, so {@code <prefix>.mutate} also gives every property's value after the change.
 * Inherited properties are included, so the node of a type offers everything that type has.
 */
public final class ReflectedType {
    private static final String TARGET = "target";
    private static final String NEXT = "next";

    /** Id of the node standing in for every reflected type's read and change nodes in the editor. */
    public static final String MUTATE = "object.mutate";

    private ReflectedType() {}

    /**
     * The node the editor shows for the read and change nodes of every reflected type: only an optional
     * {@code target}, turned into the variant of whatever is wired into it. Compiled as it is, it passes the object on.
     *
     * @return the node
     */
    public static ExpressionDefinition mutate() {
        return new ExpressionDefinition(
                NodeDescriptor.builder(MUTATE, NodeKind.PURE)
                        .category("object")
                        .name("Mutate")
                        .description("Reads the properties of whatever is wired in, and changes those given a value")
                        .input(TARGET, "Target", PortType.of(Object.class), true, "The object to read or change")
                        .output(TARGET, "Target", PortType.of(Object.class), "The object, unchanged")
                        .build(),
                (port, ctx) -> ctx.connected(TARGET) ? ctx.input(TARGET) : CodeBlock.of("null"));
    }

    /**
     * Starts deriving nodes for a class.
     *
     * @param type   the class
     * @param prefix id prefix of the derived nodes, e.g. {@code location}
     * @return a builder
     */
    public static Builder of(Class<?> type, String prefix) {
        return new Builder(type, prefix);
    }

    /**
     * Builder for the nodes of one class.
     */
    public static final class Builder {
        private final Class<?> type;
        private final String prefix;
        private final Set<String> excluded = new HashSet<>();
        private final Map<String, String> names = new HashMap<>();
        private final Map<String, String> titles = new HashMap<>();
        private String category;
        private String name;
        private final Set<String> accessors = new HashSet<>();
        private boolean getter = true;
        private boolean setters = true;
        private boolean fluent;
        private boolean recordConstructor = true;
        private final List<Creations.Creation> creations = new ArrayList<>();
        private final List<Calls.Call> calls = new ArrayList<>();

        private Builder(Class<?> type, String prefix) {
            this.type = Objects.requireNonNull(type, "type");
            this.prefix = Objects.requireNonNull(prefix, "prefix");
            this.category = prefix.contains(".") ? prefix.substring(0, prefix.indexOf('.')) : prefix;
            this.name = type.getSimpleName();
        }

        /**
         * Editor category of the derived nodes. Defaults to the first segment of the prefix.
         */
        public Builder category(String category) {
            this.category = category;
            return this;
        }

        /**
         * Name of the type in node names, e.g. "Location" in "Read Location". Defaults to the simple class name.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Leaves out properties by id; applies to getters and setters.
         */
        public Builder exclude(String... properties) {
            excluded.addAll(List.of(properties));
            return this;
        }

        /**
         * Sets the editor name of an action or query node, e.g. "Remove Entity" for {@code remove}; without, the node
         * is named after its id suffix.
         *
         * @param call  the node's id suffix
         * @param title its editor name
         * @return this builder
         */
        public Builder title(String call, String title) {
            titles.put(call, title);
            return this;
        }

        /**
         * Sets the editor name of a property.
         */
        public Builder rename(String property, String name) {
            names.put(property, name);
            return this;
        }

        /**
         * Also accepts record-style accessors: {@code x()} as a getter when a field {@code x} of its type backs it or a
         * setter {@code x(T)} pairs with it, and that setter. Off by default.
         */
        public Builder fluent() {
            this.fluent = true;
            return this;
        }

        /**
         * Names {@code x()} methods that are getters regardless of fields or pairs; their {@code x(T)} setters follow.
         */
        public Builder accessors(String... methods) {
            accessors.addAll(List.of(methods));
            return this;
        }

        /**
         * Exposes a constructor as node {@code <prefix>.<name>}, with the parameters named for the editor.
         *
         * @param name   node id suffix, e.g. {@code create}
         * @param params the constructor's parameters in order; their types select the overload
         */
        public Builder constructor(String name, Param... params) {
            creations.add(new Creations.Creation(name, null, List.of(params)));
            return this;
        }

        /**
         * Exposes a public static method of the class as node {@code <prefix>.<name>}.
         *
         * @param name   node id suffix
         * @param method the method name
         * @param params the method's parameters in order; their types select the overload
         */
        public Builder factory(String name, String method, Param... params) {
            creations.add(new Creations.Creation(name, method, List.of(params)));
            return this;
        }

        /**
         * Exposes an instance method with side effects as action node {@code <prefix>.<name>}. It runs where it is
         * chained; a return value becomes output {@code result}, readable by the chain after it.
         *
         * @param name   node id suffix, e.g. {@code add_potion_effect}
         * @param method the method name
         * @param params the method's parameters in order; their types select the overload
         */
        public Builder action(String name, String method, Param... params) {
            calls.add(new Calls.Call(name, method, List.of(params), true));
            return this;
        }

        /**
         * Exposes an instance method without side effects as pure node {@code <prefix>.<name>} with output
         * {@code result}, evaluated once per scope like a getter.
         *
         * @param name   node id suffix
         * @param method the method name
         * @param params the method's parameters in order; their types select the overload
         */
        public Builder query(String name, String method, Param... params) {
            calls.add(new Calls.Call(name, method, List.of(params), false));
            return this;
        }

        /**
         * Whether a record gets {@code <prefix>.create} from its canonical constructor. Defaults to true.
         */
        public Builder recordConstructor(boolean recordConstructor) {
            this.recordConstructor = recordConstructor;
            return this;
        }

        /**
         * Whether to derive the get node. Defaults to true.
         */
        public Builder getter(boolean getter) {
            this.getter = getter;
            return this;
        }

        /**
         * Whether to derive the set node. Defaults to true.
         */
        public Builder setters(boolean setters) {
            this.setters = setters;
            return this;
        }

        /**
         * @param rules which methods qualify
         * @return the get node, if the class has any getter, and the set node, if it has any setter
         */
        public List<NodeDefinition> build(ReflectionRules rules) {
            List<NodeDefinition> nodes = new ArrayList<>();
            PortType target = PortType.of(type);
            Properties.Style style = new Properties.Style(fluent, accessors);
            List<Properties.Getter> getters = getter
                    ? Properties.getters(type, rules, style).stream()
                            .filter(property -> !excluded.contains(property.id()))
                            .toList()
                    : List.of();
            if (!getters.isEmpty())
                nodes.add(getNode(target, grouped(getters, Properties.Getter::method, rules), rules));
            if (setters) {
                List<Properties.Setter> found = Properties.setters(type, rules, style).stream()
                        .filter(setter -> !excluded.contains(setter.property()))
                        .toList();
                if (!found.isEmpty()) {
                    nodes.add(setNode(
                            target,
                            grouped(found, Properties.Setter::method, rules),
                            grouped(getters, Properties.Getter::method, rules),
                            rules));
                }
            }
            List<Creations.Creation> all = new ArrayList<>(creations);
            if (type.isRecord()
                    && recordConstructor
                    && all.stream().noneMatch(creation -> creation.name().equals("create"))) {
                all.add(Creations.record(type));
            }
            all.forEach(creation ->
                    nodes.add(Creations.node(type, prefix, category, name, creation, rules.documentation())));
            calls.forEach(call -> nodes.add(Calls.node(
                    type,
                    prefix,
                    category,
                    name,
                    titles.getOrDefault(call.name(), Names.humanize(call.name())),
                    call,
                    rules.documentation())));
            return nodes;
        }

        /**
         * Orders properties by their group, the node's own type first and the most general last, keeping the order
         * within a group.
         */
        private <P> List<P> grouped(List<P> properties, Function<P, Method> method, ReflectionRules rules) {
            List<String> order = rules.groupOrder(type);
            return properties.stream()
                    .sorted(Comparator.comparingInt(property -> {
                        String group = rules.group(type, method.apply(property));
                        return group == null ? -1 : order.indexOf(group);
                    }))
                    .toList();
        }

        private ExpressionDefinition getNode(PortType target, List<Properties.Getter> getters, ReflectionRules rules) {
            Documentation docs = rules.documentation();
            NodeDescriptor.Builder descriptor = NodeDescriptor.builder(prefix + ".read", NodeKind.PURE)
                    .category(category)
                    .name("Read " + name)
                    .family(MUTATE)
                    .description("Reads the properties of a " + name)
                    .input(TARGET, name, target, false, docs.of(type).orElse(""));
            getters.forEach(property -> descriptor.output(
                    property.id(),
                    displayName(property.id()),
                    property.type(),
                    docs.of(property.method())
                            .or(() -> docs.returns(property.method()))
                            .orElse(""),
                    rules.group(type, property.method())));
            Map<String, String> methods = getters.stream()
                    .collect(Collectors.toMap(
                            Properties.Getter::id, property -> property.method().getName()));
            return new ExpressionDefinition(
                    descriptor.build(), (port, ctx) -> CodeBlock.of("$L.$N()", ctx.input(TARGET), methods.get(port)));
        }

        /**
         * {@code <prefix>.mutate}: every setter as an optional input, applied in declaration order when given, so one node
         * changes several properties, and every getter as an output read after the change. Overloads of a property
         * contribute their first setter. A property named like the receiver input gets the input id
         * {@code <property>_value}.
         */
        private StatementDefinition setNode(
                PortType target,
                List<Properties.Setter> setters,
                List<Properties.Getter> getters,
                ReflectionRules rules) {
            Documentation docs = rules.documentation();
            NodeDescriptor.Builder descriptor = NodeDescriptor.builder(prefix + ".mutate", NodeKind.ACTION)
                    .category(category)
                    .name("Mutate " + name)
                    .family(MUTATE)
                    .description("Changes the properties of a %s that are given, the others stay as they are, and gives"
                                    .formatted(name)
                            + " every property's value after the change")
                    .input(TARGET, name, target, false, docs.of(type).orElse(""));
            Map<String, String> methods = new LinkedHashMap<>();
            for (Properties.Setter setter : setters) {
                String input = setter.property().equals(TARGET) ? TARGET + "_value" : setter.property();
                if (methods.containsKey(input)) continue;
                methods.put(input, setter.method().getName());
                descriptor.input(
                        input,
                        displayName(setter.property()),
                        setter.type(),
                        true,
                        docs.of(setter.method()).orElse(""),
                        rules.group(type, setter.method()));
            }
            getters.forEach(property -> descriptor.output(
                    property.id(),
                    displayName(property.id()),
                    property.type(),
                    docs.of(property.method())
                            .or(() -> docs.returns(property.method()))
                            .orElse(""),
                    rules.group(type, property.method())));
            Map<String, String> reads = getters.stream()
                    .collect(Collectors.toMap(
                            Properties.Getter::id, property -> property.method().getName()));
            descriptor.execution(NEXT);
            return new StatementDefinition(descriptor.build(), ctx -> {
                CodeBlock.Builder code = CodeBlock.builder();
                methods.forEach((input, method) -> {
                    if (ctx.connected(input))
                        code.addStatement("$L.$N($L)", ctx.input(TARGET), method, ctx.input(input));
                });
                reads.forEach((output, method) -> {
                    if (!ctx.isRead(output)) return;
                    code.addStatement(
                            "final $T $L = $L.$N()",
                            ctx.type(output),
                            ctx.bind(output, NEXT),
                            ctx.input(TARGET),
                            method);
                });
                return code.add(ctx.chain(NEXT)).build();
            });
        }

        private String displayName(String property) {
            return names.getOrDefault(property, Names.humanize(property));
        }
    }
}
