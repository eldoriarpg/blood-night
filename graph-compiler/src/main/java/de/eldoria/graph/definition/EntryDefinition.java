package de.eldoria.graph.definition;

import java.util.List;
import java.util.Objects;

/**
 * Start of an execution chain. The generated class implements {@link #method()} of the host interface; its body is
 * the code chained into the entry's execution ports.
 *
 * @param descriptor id, editor texts and ports; kind {@link NodeKind#ENTRY}, no inputs or config
 * @param method     name of the host interface method
 * @param parameters parameters of that method, in order
 * @param emitter    expression for each output, usually derived from a parameter
 */
public record EntryDefinition(NodeDescriptor descriptor,
                              String method,
                              List<EntryParameter> parameters,
                              OutputEmitter<EntryContext> emitter) implements NodeDefinition {
    public EntryDefinition {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(emitter, "emitter");
        parameters = List.copyOf(parameters);
        Shapes.require(descriptor, NodeKind.ENTRY);
        if (!descriptor.inputs().isEmpty() || !descriptor.config().isEmpty()) {
            throw new IllegalArgumentException(descriptor.id() + ": entry points take no inputs or config");
        }
        if (descriptor.executions().isEmpty()) throw new IllegalArgumentException(descriptor.id() + ": entry points need an execution port");
    }
}
