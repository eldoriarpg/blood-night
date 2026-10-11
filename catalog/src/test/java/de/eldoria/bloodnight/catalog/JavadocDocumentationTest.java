/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.catalog;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavadocDocumentationTest {
    private static JavadocDocumentation docs;

    @BeforeAll
    static void open() {
        docs = new JavadocDocumentation(
                Arrays.stream(System.getProperty("catalog.sources").split(File.pathSeparator))
                        .map(Path::of)
                        .toList());
    }

    @AfterAll
    static void close() throws Exception {
        docs.close();
    }

    @Test
    void methodsAreDescribedByTheirFirstSentence() throws NoSuchMethodException {
        String text =
                docs.of(LivingEntity.class.getMethod("getAbsorptionAmount")).orElseThrow();

        assertTrue(text.toLowerCase().contains("absorption"), text);
        assertTrue(text.endsWith("."), text);
        assertTrue(!text.contains("{@"), text);
    }

    @Test
    void inheritedMethodsUseTheOverriddenJavadoc() throws NoSuchMethodException {
        assertTrue(docs.of(LivingEntity.class.getMethod("getLocation")).isPresent());
    }

    @Test
    void nestedTypesAreFound() {
        assertTrue(docs.of(EntityDamageEvent.DamageCause.class).isPresent());
    }

    @Test
    void inlineTagsBecomeText() throws NoSuchMethodException {
        String text = docs.of(EntityDamageEvent.class.getMethod("setDamage", double.class))
                .orElseThrow();
        assertEquals(-1, text.indexOf('{'), text);
    }
}
