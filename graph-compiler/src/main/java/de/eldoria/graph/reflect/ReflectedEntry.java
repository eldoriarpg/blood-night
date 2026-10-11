package de.eldoria.graph.reflect;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.EntryParameter;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Derives an entry point from a method of the host interface.
 * <p>
 * The entry's outputs are the method's parameters, each under its name, followed by the getters of each parameter's
 * class. With a single parameter the getter outputs keep their plain ids ({@code damage}); with several they are
 * prefixed by the parameter ({@code event_damage}). A getter output never replaces a parameter output of the same id.
 * <p>
 * Parameter names come from the class file when it was compiled with {@code -parameters}, otherwise from the
 * parameter type ({@code EntityDamageEvent} → {@code entity_damage_event}); {@link Builder#parameterNames} overrides
 * both.
 */
public final class ReflectedEntry {
    private static final String NEXT = "next";

    private ReflectedEntry() {
    }

    /**
     * Starts deriving an entry point.
     *
     * @param host   the host interface
     * @param method name of the host method; must not be overloaded unless {@link Builder#parameterTypes} is used
     * @param id     stable node id, e.g. {@code event.entity_damage}
     * @return a builder
     */
    public static Builder of(Class<?> host, String method, String id) {
        return new Builder(host, method, id);
    }

    /**
     * Builder for one entry point.
     */
    public static final class Builder {
        private final Class<?> host;
        private final String method;
        private final String id;
        private final Set<String> excluded = new HashSet<>();
        private final Map<String, String> names = new HashMap<>();
        private Class<?>[] parameterTypes;
        private List<String> parameterNames;
        private String category;
        private String name;
        private String description = "";
        private boolean flatten = true;

        private Builder(Class<?> host, String method, String id) {
            this.host = Objects.requireNonNull(host, "host");
            this.method = Objects.requireNonNull(method, "method");
            this.id = Objects.requireNonNull(id, "id");
            this.category = id.contains(".") ? id.substring(0, id.indexOf('.')) : id;
            this.name = Names.humanize(id.substring(id.lastIndexOf('.') + 1));
        }

        /**
         * Selects an overload of the host method.
         */
        public Builder parameterTypes(Class<?>... types) {
            this.parameterTypes = types.clone();
            return this;
        }

        /**
         * Names the parameters, in order. Names must be snake_case ids.
         */
        public Builder parameterNames(String... names) {
            this.parameterNames = List.of(names);
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Leaves out outputs by id.
         */
        public Builder exclude(String... outputs) {
            excluded.addAll(List.of(outputs));
            return this;
        }

        /**
         * Sets the editor name of an output.
         */
        public Builder rename(String output, String name) {
            names.put(output, name);
            return this;
        }

        /**
         * Whether the getters of the parameters become outputs. Defaults to true.
         */
        public Builder flatten(boolean flatten) {
            this.flatten = flatten;
            return this;
        }

        /**
         * @param rules which getters qualify
         * @return the entry definition
         * @throws IllegalArgumentException when the host method cannot be found unambiguously
         */
        public EntryDefinition build(ReflectionRules rules) {
            Method target = hostMethod();
            List<String> parameters = parameterIds(target);
            Map<String, CodeBlock> outputs = new LinkedHashMap<>();
            Map<String, PortType> types = new LinkedHashMap<>();
            List<EntryParameter> entryParameters = new ArrayList<>();

            Type[] genericTypes = target.getGenericParameterTypes();
            for (int i = 0; i < genericTypes.length; i++) {
                PortType type = PortType.of(genericTypes[i]);
                String parameter = parameters.get(i);
                entryParameters.add(new EntryParameter(parameter, type));
                types.put(parameter, type);
                outputs.put(parameter, CodeBlock.of("$N", parameter));
            }
            if (flatten) {
                for (int i = 0; i < genericTypes.length; i++) {
                    String parameter = parameters.get(i);
                    for (Properties.Getter getter : Properties.getters(target.getParameterTypes()[i], rules)) {
                        String output = genericTypes.length == 1 ? getter.id() : parameter + "_" + getter.id();
                        if (types.containsKey(output)) continue;
                        types.put(output, getter.type());
                        outputs.put(output, CodeBlock.of("$N.$N()", parameter, getter.method().getName()));
                    }
                }
            }

            NodeDescriptor.Builder descriptor = NodeDescriptor.builder(id, NodeKind.ENTRY)
                    .category(category)
                    .name(name)
                    .description(description)
                    .execution(NEXT);
            types.forEach((output, type) -> {
                if (!excluded.contains(output)) descriptor.output(output, names.getOrDefault(output, Names.humanize(output)), type);
            });
            return new EntryDefinition(descriptor.build(), method, entryParameters, (port, ctx) -> {
                CodeBlock code = outputs.get(port);
                if (code == null) throw new IllegalArgumentException(id + " has no output " + port);
                return code;
            });
        }

        private Method hostMethod() {
            if (parameterTypes != null) {
                try {
                    return host.getMethod(method, parameterTypes);
                } catch (NoSuchMethodException e) {
                    throw new IllegalArgumentException("%s has no method %s%s".formatted(host.getName(), method, Arrays.toString(parameterTypes)), e);
                }
            }
            List<Method> candidates = Arrays.stream(host.getMethods()).filter(candidate -> candidate.getName().equals(method)).toList();
            if (candidates.size() != 1) {
                throw new IllegalArgumentException("%s has %d methods named %s; select one with parameterTypes".formatted(host.getName(), candidates.size(), method));
            }
            return candidates.getFirst();
        }

        private List<String> parameterIds(Method target) {
            if (parameterNames != null) {
                if (parameterNames.size() != target.getParameterCount()) {
                    throw new IllegalArgumentException("%s takes %d parameters, %d names given".formatted(method, target.getParameterCount(), parameterNames.size()));
                }
                return parameterNames;
            }
            List<String> ids = new ArrayList<>();
            for (Parameter parameter : target.getParameters()) {
                String base = Properties.snakeCase(parameter.isNamePresent() ? parameter.getName() : parameter.getType().getSimpleName());
                String unique = base;
                for (int suffix = 2; ids.contains(unique); suffix++) unique = base + suffix;
                ids.add(unique);
            }
            return ids;
        }
    }
}
