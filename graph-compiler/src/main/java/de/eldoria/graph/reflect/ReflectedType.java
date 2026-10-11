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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Derives nodes from the public getters and setters of a class.
 * <p>
 * For a class registered under prefix {@code location}:
 * <ul>
 *     <li>{@code location.get}: a pure node taking the object as {@code target}, one output per getter;</li>
 *     <li>{@code location.set_x}, …: one action per setter, taking {@code target} and the new value.</li>
 * </ul>
 * Setters act on whatever object is connected to {@code target}. When that object is an entry's output, such as an
 * event, the scope rules of the compiler already restrict the setter to that entry's chain.
 */
public final class ReflectedType {
    private static final String TARGET = "target";
    private static final String NEXT = "next";

    private ReflectedType() {}

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
         * Name of the type in node names, e.g. "Location" in "Get Location". Defaults to the simple class name.
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
         * Whether to derive set nodes. Defaults to true.
         */
        public Builder setters(boolean setters) {
            this.setters = setters;
            return this;
        }

        /**
         * @param rules which methods qualify
         * @return the get node, if the class has any getter, followed by one node per setter
         */
        public List<NodeDefinition> build(ReflectionRules rules) {
            List<NodeDefinition> nodes = new ArrayList<>();
            PortType target = PortType.of(type);
            Properties.Style style = new Properties.Style(fluent, accessors);
            if (getter) {
                List<Properties.Getter> getters = Properties.getters(type, rules, style).stream()
                        .filter(property -> !excluded.contains(property.id()))
                        .toList();
                if (!getters.isEmpty()) nodes.add(getNode(target, getters, rules.documentation()));
            }
            if (setters) {
                Properties.setters(type, rules, style).stream()
                        .filter(setter -> !excluded.contains(setter.property()))
                        .map(property -> setNode(target, property, rules.documentation()))
                        .forEach(nodes::add);
            }
            List<Creations.Creation> all = new ArrayList<>(creations);
            if (type.isRecord()
                    && recordConstructor
                    && all.stream().noneMatch(creation -> creation.name().equals("create"))) {
                all.add(Creations.record(type));
            }
            all.forEach(creation ->
                    nodes.add(Creations.node(type, prefix, category, name, creation, rules.documentation())));
            calls.forEach(call -> nodes.add(Calls.node(type, prefix, category, name, call, rules.documentation())));
            return nodes;
        }

        private ExpressionDefinition getNode(PortType target, List<Properties.Getter> getters, Documentation docs) {
            NodeDescriptor.Builder descriptor = NodeDescriptor.builder(prefix + ".get", NodeKind.PURE)
                    .category(category)
                    .name("Get " + name)
                    .description("Reads the properties of a " + name)
                    .input(TARGET, name, target, false, docs.of(type).orElse(""));
            getters.forEach(property -> descriptor.output(
                    property.id(),
                    displayName(property.id()),
                    property.type(),
                    docs.of(property.method()).orElse("")));
            Map<String, String> methods = getters.stream()
                    .collect(Collectors.toMap(
                            Properties.Getter::id, property -> property.method().getName()));
            return new ExpressionDefinition(
                    descriptor.build(), (port, ctx) -> CodeBlock.of("$L.$N()", ctx.input(TARGET), methods.get(port)));
        }

        private StatementDefinition setNode(PortType target, Properties.Setter setter, Documentation docs) {
            String property = setter.property();
            String doc = docs.of(setter.method()).orElse("");
            NodeDescriptor descriptor = NodeDescriptor.builder(prefix + "." + setter.id(), NodeKind.ACTION)
                    .category(category)
                    .name("Set %s %s".formatted(name, displayName(property)))
                    .description(
                            doc.isEmpty()
                                    ? "Calls %s on a %s"
                                            .formatted(setter.method().getName(), name)
                                    : doc)
                    .input(TARGET, name, target, false, docs.of(type).orElse(""))
                    .input("value", displayName(property), setter.type(), false, doc)
                    .execution(NEXT)
                    .build();
            String method = setter.method().getName();
            return new StatementDefinition(descriptor, ctx -> CodeBlock.builder()
                    .addStatement("$L.$N($L)", ctx.input(TARGET), method, ctx.input("value"))
                    .add(ctx.chain(NEXT))
                    .build());
        }

        private String displayName(String property) {
            return names.getOrDefault(property, Names.humanize(property));
        }
    }
}
