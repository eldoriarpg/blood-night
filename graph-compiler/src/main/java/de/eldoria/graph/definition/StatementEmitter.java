package de.eldoria.graph.definition;

import com.palantir.javapoet.CodeBlock;

/**
 * Emits the statements of a node in an execution chain.
 */
@FunctionalInterface
public interface StatementEmitter {
    /**
     * @param context emit context of the node instance
     * @return statements, including the chained code the node places
     */
    CodeBlock emit(StatementContext context);
}
