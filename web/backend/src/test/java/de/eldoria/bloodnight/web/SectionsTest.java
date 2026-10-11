/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.JsonNode;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SectionsTest {
    private static final JsonNode PAYLOAD = json("""
            {'mobs':{'a':{'id':'a','attributes':{}},'b':{'id':'b','attributes':{}}},
             'macros':[{'id':'burst','name':'b','body':{}},{'id':'jump','name':'j','body':{}}],
             'settings':{'main':{'x':0},'globalDrops':{},'worlds':{'w1':{'settings':{}}}},
             'items':{},'hashes':{}}
            """);

    private static JsonNode json(String json) {
        return EditorApp.MAPPER.readTree(json.replace('\'', '"'));
    }

    private static Sections.Touched touched(String operations) {
        return Sections.of(PAYLOAD, json(operations));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "[{'op':'replace','path':'/mobs/a/attributes','value':{}}]|mobs/a",
                "[{'op':'add','path':'/mobs/c','value':{'id':'c'}}]|mobs/c",
                "[{'op':'replace','path':'/macros/1/name','value':'x'}]|macros/jump",
                "[{'op':'add','path':'/macros/-','value':{'id':'dash'}}]|macros/dash",
                "[{'op':'remove','path':'/macros/0'}]|macros/burst",
                "[{'op':'replace','path':'/settings/main/x','value':1}]|settings/main",
                "[{'op':'add','path':'/settings/globalDrops/x','value':1}]|settings/globalDrops",
                "[{'op':'replace','path':'/settings/worlds/w1/settings','value':{}}]|settings/worlds/w1"
            })
    void pointersNameTheirSection(String operations, String section) {
        assertEquals(new Sections.Touched(Set.of(section), false), touched(operations));
    }

    @Test
    void indexesFollowEarlierOperations() {
        var touched = touched("""
                [{'op':'remove','path':'/macros/0'},{'op':'replace','path':'/macros/0/name','value':'x'}]
                """);
        assertEquals(Set.of("macros/burst", "macros/jump"), touched.sections());
    }

    @Test
    void movesTouchBothEnds() {
        assertEquals(
                Set.of("mobs/a", "mobs/b"),
                touched("[{'op':'move','from':'/mobs/a/attributes','path':'/mobs/b/attributes'}]")
                        .sections());
    }

    @Test
    void pointersAboveSectionsTouchEverything() {
        assertTrue(touched("[{'op':'replace','path':'','value':{}}]").everything());
        assertTrue(touched("[{'op':'replace','path':'/mobs','value':{}}]").everything());
        assertTrue(touched("[{'op':'replace','path':'/settings/worlds','value':{}}]")
                .everything());
    }

    @Test
    void itemsAndHashesCannotChange() {
        assertThrows(JsonPatch.PatchException.class, () -> touched("[{'op':'add','path':'/items/x','value':{}}]"));
        assertThrows(JsonPatch.PatchException.class, () -> touched("[{'op':'remove','path':'/hashes'}]"));
    }
}
