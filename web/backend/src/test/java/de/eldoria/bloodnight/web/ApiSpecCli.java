/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the API description for {@code ./gradlew :web-backend:generateApiSpec}.
 */
public final class ApiSpecCli {
    private ApiSpecCli() {}

    public static void main(String[] args) throws IOException {
        Path target = args.length > 0 ? Path.of(args[0]) : ApiSpec.PATH;
        Files.createDirectories(target.toAbsolutePath().getParent());
        Files.writeString(target, ApiSpec.generate());
        System.out.println("Wrote " + target.toAbsolutePath());
    }
}
