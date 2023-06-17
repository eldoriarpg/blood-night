package de.eldoria.bloodnight.nodes.value;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.nodes.base.Node;
import de.eldoria.bloodnight.nodes.base.io.Edge;
import de.eldoria.bloodnight.nodes.base.io.EditorMeta;
import de.eldoria.bloodnight.nodes.base.io.OutputContainer;
import de.eldoria.bloodnight.nodes.meta.Fields;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base node for nodes providing values which are defined by the user.
 */
public abstract non-sealed class ValueNode extends Node {
    private final Object value;
    private MethodHandle constructorHandle;

    public ValueNode(Object value, Map<String, Edge> input, EditorMeta meta) {
        super(input, meta);
        this.value = value;
    }

    public ValueNode(Object value) {
        this.value = value;
    }

    @Override
    public OutputContainer output() {
        return super.output().set(Fields.VALUE, value);
    }

    @JsonProperty
    public Object value() {
        return value;
    }

    @Override
    public Node copy() {
        if (constructorHandle == null) {
            try {
                Class<?> clazz = null;
                for (Constructor<?> constructor : getClass().getConstructors()) {
                    if (constructor.getParameterCount() == 3) {
                        Parameter found = null;
                        for (Parameter parameter : constructor.getParameters()) {
                            JsonProperty annotation = parameter.getAnnotation(JsonProperty.class);
                            if (annotation.value().equals("value")) {
                                found = parameter;
                                break;
                            }
                        }
                        if (found == null) throw new IllegalStateException("No field annotated with value");
                        clazz = found.getType();
                        break;
                    }
                }
                if (clazz == null) throw new IllegalStateException("No matching constructor for deserialization");
                var lookup = MethodHandles.publicLookup();
                MethodType mt = MethodType.methodType(void.class, List.of(clazz, Map.class, EditorMeta.class));
                constructorHandle = lookup.findConstructor(getClass(), mt);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return (Node) constructorHandle.invoke(value(), new HashMap<>(input().edges()), meta());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
