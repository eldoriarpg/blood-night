/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

import com.palantir.javapoet.CodeBlock;

import java.util.Optional;

/**
 * Decides per entry node whether its chains run for a call of the host method, e.g. a kill entry set to players only.
 * Two entries of the same type in one graph share the host method, so the condition wraps the chains of its own
 * entry and leaves the others alone.
 */
@FunctionalInterface
public interface EntryGuard {
    /** Lets every call through. */
    EntryGuard NONE = ctx -> Optional.empty();

    /**
     * @param ctx the entry's context: its parameters and config
     * @return the condition under which its chains run, or empty when they always run
     */
    Optional<CodeBlock> condition(EntryContext ctx);
}
