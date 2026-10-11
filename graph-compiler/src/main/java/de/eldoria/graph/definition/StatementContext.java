package de.eldoria.graph.definition;

import com.palantir.javapoet.CodeBlock;

/**
 * Emit context for statements, which may contain chained code and bind their outputs.
 */
public interface StatementContext extends EmitContext {
    /**
     * Code of every node chained into an execution port, in order. Emitted in the current scope, so outputs bound
     * before this call are visible to it.
     *
     * @param port execution port id
     * @return statements; empty when nothing is chained
     */
    CodeBlock chain(String port);

    /**
     * Reserves a local variable name for an output. The definition declares and assigns the variable itself, e.g. as
     * a loop variable, before chaining code that reads it.
     *
     * @param port output port id
     * @return the variable name
     */
    CodeBlock bind(String port);
}
