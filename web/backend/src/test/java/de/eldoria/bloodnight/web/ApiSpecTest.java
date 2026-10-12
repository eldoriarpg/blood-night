/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.web.api.assets.AssetRoutes;
import de.eldoria.bloodnight.web.api.editor.EditorRoutes;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiSpecTest {
    private static final Path COMMITTED = Path.of("../..").resolve(ApiSpec.PATH);

    @Test
    void everyRouteIsDescribed() {
        Set<String> routes = Stream.of(EditorRoutes.class, AssetRoutes.class)
                .flatMap(routeClass -> Arrays.stream(routeClass.getFields()))
                .filter(field -> Modifier.isStatic(field.getModifiers()) && field.getType() == String.class)
                .map(ApiSpecTest::value)
                .filter(route -> !route.equals(EditorRoutes.STREAM))
                .collect(Collectors.toSet());
        Set<String> described =
                ApiSpec.OPERATIONS.stream().map(ApiSpec.Operation::route).collect(Collectors.toSet());
        assertEquals(routes, described);
    }

    @Test
    void theCommittedDescriptionIsCurrent() throws IOException {
        assertTrue(Files.exists(COMMITTED), "Run ./gradlew :web-backend:generateApiSpec");
        assertEquals(
                ApiSpec.generate(),
                Files.readString(COMMITTED),
                "The API description is stale, run ./gradlew :web-backend:generateApiSpec");
    }

    private static String value(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
