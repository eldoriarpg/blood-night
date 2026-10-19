/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler.emit;

import com.palantir.javapoet.CodeBlock;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final @Nullable Scope parent;
    private final boolean isolating;
    private final @Nullable String entry;
    private final Map<String, CodeBlock> computed = new HashMap<>();
    private final Map<String, CodeBlock> bound = new HashMap<>();
    private final Set<String> names;
    private final List<Scope> children = new ArrayList<>();
    private boolean open = true;

    private Scope(@Nullable Scope parent, boolean isolating, @Nullable String entry, Set<String> names) {
        this.parent = parent;
        this.isolating = isolating;
        this.entry = entry;
        this.names = names;
    }

    /**
     * @param entry       id of the entry node whose method body this scope is
     * @param methodNames names declared at the top level of the method, shared by every entry the method runs, so
     *                    the method parameters and the locals of earlier entries stay taken
     * @return the root scope of an entry method
     */
    static Scope root(String entry, Set<String> methodNames) {
        return new Scope(null, false, entry, methodNames);
    }

    /**
     * @param isolating whether computed values of enclosing scopes stay invisible
     * @return a nested scope
     */
    Scope child(boolean isolating) {
        Scope child = new Scope(this, isolating, null, new HashSet<>());
        children.add(child);
        return child;
    }

    /**
     * Whether a local of that name can be declared here. Java forbids redeclaring a name that is visible, and
     * declaring one that a nested block still to be closed declares as well. Blocks that are finished do not count,
     * so sibling blocks reuse names.
     *
     * @param name a local name
     * @return whether it is free here
     */
    boolean free(String name) {
        for (Scope scope = this; scope != null; scope = scope.parent) {
            if (scope.names.contains(name)) return false;
        }
        return freeBelow(name);
    }

    private boolean freeBelow(String name) {
        for (Scope child : children) {
            if (child.open && (child.names.contains(name) || !child.freeBelow(name))) return false;
        }
        return true;
    }

    /**
     * @param name a local name declared in this scope
     */
    void declare(String name) {
        names.add(name);
    }

    /**
     * Marks the nested blocks created so far as finished: the code that follows comes after them.
     */
    void closeChildren() {
        children.forEach(child -> child.open = false);
    }

    static String key(String node, String port) {
        return node + '\u0000' + port;
    }

    @Nullable
    CodeBlock findComputed(String key) {
        for (Scope scope = this; scope != null; scope = scope.parent) {
            CodeBlock value = scope.computed.get(key);
            if (value != null) return value;
            if (scope.isolating) return null;
        }
        return null;
    }

    @Nullable
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

    /**
     * @param key value key
     * @return whether this very scope, not an enclosing one, computed the value
     */
    boolean holds(String key) {
        return computed.containsKey(key);
    }

    void compute(String key, CodeBlock local) {
        computed.put(key, local);
    }

    void bind(String key, CodeBlock local) {
        bound.put(key, local);
    }
}
