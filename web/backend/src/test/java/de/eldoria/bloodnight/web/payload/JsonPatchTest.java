/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.payload;

import de.eldoria.bloodnight.web.EditorApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonPatchTest {
    private static JsonNode json(String json) {
        return EditorApp.MAPPER.readTree(json.replace('\'', '"'));
    }

    private static String patch(String document, String operations) {
        return JsonPatch.apply(json(document), json(operations)).toString().replace('"', '\'');
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "{'a':1}|[{'op':'add','path':'/b','value':2}]|{'a':1,'b':2}",
                "{'a':[1,3]}|[{'op':'add','path':'/a/1','value':2}]|{'a':[1,2,3]}",
                "{'a':[1]}|[{'op':'add','path':'/a/-','value':2}]|{'a':[1,2]}",
                "{'a':1,'b':2}|[{'op':'remove','path':'/a'}]|{'b':2}",
                "{'a':[1,2,3]}|[{'op':'remove','path':'/a/1'}]|{'a':[1,3]}",
                "{'a':1}|[{'op':'replace','path':'/a','value':5}]|{'a':5}",
                "{'a':{'x':1},'b':{}}|[{'op':'move','from':'/a/x','path':'/b/y'}]|{'a':{},'b':{'y':1}}",
                "{'a':{'x':1}}|[{'op':'copy','from':'/a','path':'/b'}]|{'a':{'x':1},'b':{'x':1}}",
                "{'a~b':{'c/d':1}}|[{'op':'replace','path':'/a~0b/c~1d','value':2}]|{'a~b':{'c/d':2}}",
                "{'a':1}|[{'op':'replace','path':'','value':{'z':0}}]|{'z':0}",
                "{'a':1}|[{'op':'test','path':'/a','value':1},{'op':'add','path':'/b','value':2}]|{'a':1,'b':2}"
            })
    void operationsFollowTheRfc(String document, String operations, String expected) {
        assertEquals(expected, patch(document, operations));
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "{'a':1}|[{'op':'remove','path':'/b'}]",
                "{'a':1}|[{'op':'replace','path':'/b','value':1}]",
                "{'a':[1]}|[{'op':'add','path':'/a/5','value':1}]",
                "{'a':[1]}|[{'op':'add','path':'/a/01','value':1}]",
                "{'a':1}|[{'op':'test','path':'/a','value':2}]",
                "{'a':{}}|[{'op':'move','from':'/a','path':'/a/b'}]",
                "{'a':1}|[{'op':'jump','path':'/a'}]",
                "{'a':1}|[{'op':'add','path':'a','value':1}]",
                "{'a':1}|{'op':'add'}"
            })
    void badOperationsAreRefused(String document, String operations) {
        assertThrows(JsonPatch.PatchException.class, () -> patch(document, operations));
    }

    @Test
    void aFailingOperationChangesNothing() {
        JsonNode document = json("{'a':1}");
        assertThrows(
                JsonPatch.PatchException.class,
                () -> JsonPatch.apply(
                        document, json("[{'op':'add','path':'/b','value':2},{'op':'remove','path':'/c'}]")));
        assertEquals(json("{'a':1}"), document);
    }
}
