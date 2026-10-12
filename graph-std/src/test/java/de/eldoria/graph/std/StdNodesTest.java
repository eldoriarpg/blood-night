/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.types.TypeMapping;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StdNodesTest {
    /**
     * @param constants an interface of id constants
     * @return the values of its string constants, inherited ones included
     */
    static Set<String> ids(Class<?> constants) {
        return Arrays.stream(constants.getFields())
                .filter(field -> field.getType() == String.class)
                .map(StdNodesTest::value)
                .collect(Collectors.toSet());
    }

    private static String value(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void everyStandardNodeHasExactlyOneConstant() {
        Set<String> defined = Stream.concat(
                        StandardNodes.all().stream(), StandardNodes.typed(TypeMapping.defaults()).stream())
                .map(NodeDefinition::id)
                .collect(Collectors.toSet());
        assertEquals(defined, ids(StdNodes.class));
        assertEquals(StdNodes.class.getFields().length, ids(StdNodes.class).size());
    }
}
