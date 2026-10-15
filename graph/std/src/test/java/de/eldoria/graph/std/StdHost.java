/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

/**
 * Host interface for testing standard nodes.
 */
public interface StdHost {
    void run();

    /**
     * The clock, called by tests that wait.
     */
    default void tick() {}
}
