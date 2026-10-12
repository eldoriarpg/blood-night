/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

import java.util.List;
import java.util.Objects;

/**
 * Start of an execution chain. The generated class implements {@link #method()} of the host interface; its body is
 * the code chained into the entry's execution ports.
 *
 * @param descriptor id, editor texts and ports; kind {@link NodeKind#ENTRY}, no inputs
 * @param method     name of the host interface method
 * @param parameters parameters of that method, in order
 * @param emitter    expression for each output, usually derived from a parameter
 * @param guard      when the entry's chains run, e.g. from a config field; {@link EntryGuard#NONE} for always
 */
public record EntryDefinition(
        NodeDescriptor descriptor,
        String method,
        List<EntryParameter> parameters,
        OutputEmitter<EntryContext> emitter,
        EntryGuard guard)
        implements NodeDefinition {
    /**
     * An entry whose chains run on every call of the host method.
     */
    public EntryDefinition(
            NodeDescriptor descriptor,
            String method,
            List<EntryParameter> parameters,
            OutputEmitter<EntryContext> emitter) {
        this(descriptor, method, parameters, emitter, EntryGuard.NONE);
    }

    public EntryDefinition {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(emitter, "emitter");
        Objects.requireNonNull(guard, "guard");
        parameters = List.copyOf(parameters);
        Shapes.require(descriptor, NodeKind.ENTRY);
        if (!descriptor.inputs().isEmpty()) {
            throw new IllegalArgumentException(descriptor.id() + ": entry points take no inputs");
        }
        if (descriptor.executions().isEmpty())
            throw new IllegalArgumentException(descriptor.id() + ": entry points need an execution port");
    }
}
