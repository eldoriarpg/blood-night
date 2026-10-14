/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.reflect;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.EmitContext;

/**
 * Wraps what the read and mutate nodes of a reflected type emit on their receiver, for platforms where a call is only
 * allowed in some places: e.g. running changes on another thread, or refusing a read with a clear failure.
 */
public interface CallGuard {
    /**
     * Emits every call as it is.
     */
    CallGuard NONE = new CallGuard() {};

    /**
     * @param ctx    the node's emit context
     * @param method the getter's name
     * @param call   the plain call on the receiver
     * @return the expression to emit for the read
     */
    default CodeBlock read(EmitContext ctx, String method, CodeBlock call) {
        return call;
    }

    /**
     * @param ctx      the node's emit context
     * @param receiver a final local holding the receiver
     * @param changes  statements applying the given setters to the receiver; they only read final locals, so they may
     *                 run later or in a lambda
     * @return the statements to emit instead
     */
    default CodeBlock write(EmitContext ctx, CodeBlock receiver, CodeBlock changes) {
        return changes;
    }
}
