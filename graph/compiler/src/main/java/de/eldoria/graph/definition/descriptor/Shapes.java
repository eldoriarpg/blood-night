/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition.descriptor;

import java.util.Set;

/**
 * Checks that a descriptor's kind fits the definition shape it is used with.
 */
public final class Shapes {
    private Shapes() {}

    public static void require(NodeDescriptor descriptor, NodeKind... kinds) {
        if (!Set.of(kinds).contains(descriptor.kind())) {
            throw new IllegalArgumentException("%s: kind %s does not fit this definition, expected one of %s"
                    .formatted(descriptor.id(), descriptor.kind(), Set.of(kinds)));
        }
    }
}
