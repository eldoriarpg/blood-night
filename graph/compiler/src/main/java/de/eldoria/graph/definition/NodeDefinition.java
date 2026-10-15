/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

/**
 * A node type: descriptor plus code emitter. The shape decides how the compiler places the code.
 */
public sealed interface NodeDefinition
        permits ExpressionDefinition, StatementDefinition, EntryDefinition, MacroDefinition {
    /**
     * @return id, editor texts and ports
     */
    NodeDescriptor descriptor();

    /**
     * @return the stable id of this node type
     */
    default String id() {
        return descriptor().id();
    }
}
