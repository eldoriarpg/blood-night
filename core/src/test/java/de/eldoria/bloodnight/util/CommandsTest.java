/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandsTest {
    @Test
    void aLeadingSlashAndSpacesAreDropped() {
        assertEquals("say hi", Commands.withoutSlash("/say hi"));
        assertEquals("say hi", Commands.withoutSlash("  / say hi "));
        assertEquals("say /hi", Commands.withoutSlash("say /hi"));
    }
}
