/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.std.StdNodes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodNightNodesTest {
    @Test
    void everyConstantNamesANodeOfThePlatform() {
        GraphPlatform platform =
                BloodNightPlatform.create(CompileClasspath.discover(BloodNightNodesTest.class.getClassLoader()));
        List<String> unknown = Arrays.stream(BloodNightNodes.class.getFields())
                .map(BloodNightNodesTest::value)
                .filter(id -> platform.node(id).isEmpty())
                .toList();
        assertEquals(List.of(), unknown);
        assertTrue(platform.node(StdNodes.cast("potion_effect_type")).isPresent());
    }

    private static String value(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
