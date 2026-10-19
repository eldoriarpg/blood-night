/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std.runtime.flow;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Picks the case of a switch node. Generated code holds no state for it.
 */
public final class Switches {
    private Switches() {}

    /**
     * @param value the value switched on
     * @param cases the case values, in the order the switch lists them
     * @return the position of the first case equal to the value, or -1 when none is
     */
    public static int match(@Nullable Object value, Object... cases) {
        for (int i = 0; i < cases.length; i++) {
            if (Objects.equals(value, cases[i])) return i;
        }
        return -1;
    }
}
