/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayloadSchemaTest {
    private static final Path COMMITTED = Path.of("../..").resolve(PayloadSchema.PATH);

    @Test
    void theCommittedSchemaIsCurrent() throws IOException {
        assertTrue(Files.exists(COMMITTED), "Run ./gradlew :core:generatePayloadSchema");
        assertEquals(
                PayloadSchema.generate(),
                Files.readString(COMMITTED),
                "The payload schema is stale, run ./gradlew :core:generatePayloadSchema");
    }
}
