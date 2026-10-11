/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

import java.util.ArrayList;
import java.util.List;

/**
 * Context collecting the values a test graph records.
 */
public final class Recorder {
    private final List<Object> values = new ArrayList<>();

    public void record(Object value) {
        values.add(value);
    }

    public List<Object> values() {
        return values;
    }
}
