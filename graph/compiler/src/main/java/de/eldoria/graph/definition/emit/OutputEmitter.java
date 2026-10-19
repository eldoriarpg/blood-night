/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition.emit;

import com.palantir.javapoet.CodeBlock;

/**
 * Emits the expression for one output of a node.
 *
 * @param <C> the context type
 */
@FunctionalInterface
public interface OutputEmitter<C extends EmitContext> {
    /**
     * @param port    output port id
     * @param context emit context of the node instance
     * @return an expression of the output's type
     */
    CodeBlock output(String port, C context);
}
