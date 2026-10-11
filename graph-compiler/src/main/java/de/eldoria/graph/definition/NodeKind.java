/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

/**
 * What a node is, as the editor groups it. Must agree with the shape of the definition.
 */
public enum NodeKind {
    /**
     * An expression without inputs that produces literal values from its config.
     */
    VALUE,
    /**
     * An expression computed from its inputs, without side effects.
     */
    PURE,
    /**
     * A statement in an execution chain.
     */
    ACTION,
    /**
     * A statement that places chained code into nested blocks: branches, loops, sequences.
     */
    CONTROL_FLOW,
    /**
     * Start of an execution chain; a method the host calls.
     */
    ENTRY
}
