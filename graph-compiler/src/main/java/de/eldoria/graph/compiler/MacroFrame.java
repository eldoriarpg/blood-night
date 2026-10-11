/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

/**
 * One step into a macro: which macro, and which node inside its body.
 *
 * @param macro id of the macro
 * @param node  id of the node inside the macro body
 */
public record MacroFrame(String macro, String node) {}
