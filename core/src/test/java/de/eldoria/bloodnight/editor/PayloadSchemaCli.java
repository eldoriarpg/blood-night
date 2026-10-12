/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the editor payload's JSON Schema for {@code ./gradlew :core:generatePayloadSchema}.
 */
public final class PayloadSchemaCli {
    private PayloadSchemaCli() {}

    public static void main(String[] args) throws IOException {
        Path target = args.length > 0 ? Path.of(args[0]) : PayloadSchema.PATH;
        Files.createDirectories(target.toAbsolutePath().getParent());
        Files.writeString(target, PayloadSchema.generate());
        System.out.println("Wrote " + target.toAbsolutePath());
    }
}
