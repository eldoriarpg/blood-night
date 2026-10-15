/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

import com.palantir.javapoet.CodeBlock;

/**
 * Emit context for entry points, whose outputs are derived from the parameters of the host method.
 */
public interface EntryContext extends EmitContext {
    /**
     * @param name parameter name as declared by the entry definition
     * @return an expression referring to the parameter
     */
    CodeBlock parameter(String name);
}
