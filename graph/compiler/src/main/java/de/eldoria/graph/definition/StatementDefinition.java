/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

import java.util.Objects;

/**
 * A node placed in an execution chain: actions and control flow.
 *
 * @param descriptor id, editor texts and ports; kind {@link NodeKind#ACTION} or {@link NodeKind#CONTROL_FLOW}
 * @param emitter    statements of the node
 */
public record StatementDefinition(NodeDescriptor descriptor, StatementEmitter emitter) implements NodeDefinition {
    public StatementDefinition {
        Objects.requireNonNull(emitter, "emitter");
        Shapes.require(descriptor, NodeKind.ACTION, NodeKind.CONTROL_FLOW);
    }
}
