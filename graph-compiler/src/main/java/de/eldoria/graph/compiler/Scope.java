package de.eldoria.graph.compiler;

import com.palantir.javapoet.CodeBlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Which values generated code can see at one point, mirroring the nesting of the emitted blocks.
 * <p>
 * Two kinds of values live here. Computed values are locals holding an expression node's output; they are reused by
 * later reads in the same or a nested scope, but not across an isolating scope, so a loop body computes them afresh.
 * Bound values are statement outputs such as loop variables, placed into the chain scopes the statement names; they
 * are visible in every scope nested below.
 * <p>
 * Root scopes remember their entry node, because an entry's outputs are only readable inside its own method.
 */
final class Scope {
    private final Scope parent;
    private final boolean isolating;
    private final String entry;
    private final Map<String, CodeBlock> computed = new HashMap<>();
    private final Map<String, CodeBlock> bound = new HashMap<>();

    private Scope(Scope parent, boolean isolating, String entry) {
        this.parent = parent;
        this.isolating = isolating;
        this.entry = entry;
    }

    /**
     * @param entry id of the entry node whose method body this scope is
     * @return the root scope of an entry method
     */
    static Scope root(String entry) {
        return new Scope(null, false, entry);
    }

    /**
     * @param isolating whether computed values of enclosing scopes stay invisible
     * @return a nested scope
     */
    Scope child(boolean isolating) {
        return new Scope(this, isolating, null);
    }

    static String key(String node, String port) {
        return node + '\u0000' + port;
    }

    CodeBlock findComputed(String key) {
        for (Scope scope = this; scope != null; scope = scope.parent) {
            CodeBlock value = scope.computed.get(key);
            if (value != null) return value;
            if (scope.isolating) return null;
        }
        return null;
    }

    CodeBlock findBound(String key) {
        for (Scope scope = this; scope != null; scope = scope.parent) {
            CodeBlock value = scope.bound.get(key);
            if (value != null) return value;
        }
        return null;
    }

    /**
     * @param entry id of an entry node
     * @return whether this scope lies inside that entry's method
     */
    boolean within(String entry) {
        for (Scope scope = this; scope != null; scope = scope.parent) {
            if (entry.equals(scope.entry)) return true;
        }
        return false;
    }

    void compute(String key, CodeBlock local) {
        computed.put(key, local);
    }

    void bind(String key, CodeBlock local) {
        bound.put(key, local);
    }
}
