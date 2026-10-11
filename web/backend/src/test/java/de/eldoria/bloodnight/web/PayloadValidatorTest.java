/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.web.api.editor.PayloadProblem;
import de.eldoria.graph.payload.CatalogGenerator;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayloadValidatorTest {
    private static final String VALID = """
            {"items":{"sword":{}},
             "macros":[{"id":"burst","body":{"nodes":{}}}],
             "mobs":{"hot":{"id":"hot","attributes":{"entityType":"ZOMBIE"},
               "equipment":{"mainHand":"sword"},
               "behaviour":{"nodes":{
                 "tick":{"type":"mob.tick","executions":{"next":["burn"]}},
                 "burn":{"type":"entity.burn","inputs":{"ticks":{"node":"five","port":"value"}},"config":{"mode":1},
                         "executions":{"next":["macro"]}},
                 "five":{"type":"value.number"},
                 "macro":{"type":"macro.burst"}}}}},
             "settings":{}}
            """;

    private static List<PayloadProblem> validate(String json) {
        JsonNode payload = EditorApp.MAPPER.readTree(json);
        return new PayloadValidator(Catalogs.small()).validate(payload);
    }

    private static List<String> paths(String json) {
        return validate(json).stream().map(PayloadProblem::path).toList();
    }

    @Test
    void aValidPayloadHasNoProblems() {
        assertEquals(List.of(), validate(VALID));
    }

    @Test
    void unknownNodesPortsAndTargetsAreFound() {
        String broken = VALID.replace("\"type\":\"value.number\"", "\"type\":\"value.text\"")
                .replace("\"next\":[\"burn\"]", "\"next\":[\"gone\"]")
                .replace("\"config\":{\"mode\":1}", "\"config\":{\"speed\":1}")
                .replace("\"macro.burst\"", "\"macro.unknown\"");
        assertEquals(
                List.of(
                        "/mobs/hot/behaviour/nodes/burn/config/speed",
                        "/mobs/hot/behaviour/nodes/five/type",
                        "/mobs/hot/behaviour/nodes/macro/type",
                        "/mobs/hot/behaviour/nodes/tick/executions/next"),
                paths(broken).stream().sorted().toList());
    }

    @Test
    void connectionsNeedExistingOutputs() {
        String broken = VALID.replace("\"port\":\"value\"", "\"port\":\"result\"");
        assertEquals(List.of("/mobs/hot/behaviour/nodes/burn/inputs/ticks"), paths(broken));
    }

    @Test
    void itemsMustBeRegistered() {
        assertEquals(
                List.of("/mobs/hot/equipment/mainHand"),
                paths(VALID.replace("\"mainHand\":\"sword\"", "\"mainHand\":\"axe\"")));
    }

    @Test
    void theShapeIsChecked() {
        assertTrue(paths("[]").contains(""));
        assertEquals(
                List.of("/mobs/x/id", "/mobs/x/attributes/entityType"), paths("{\"mobs\":{\"x\":{\"id\":\"y\"}}}"));
    }

    @Test
    void catalogsWithoutEmptyListsAreRead() {
        var catalog = CatalogGenerator.read("""
                {"format":1,"version":"v","nodes":[{"id":"mob.tick","kind":"ENTRY","category":"mob","name":"Tick",
                 "description":"","macroOnly":false}],"types":[],"selectable":{}}
                """);
        var payload = EditorApp.MAPPER.readTree(
                "{\"mobs\":{\"a\":{\"id\":\"a\",\"attributes\":{\"entityType\":\"ZOMBIE\"},"
                        + "\"behaviour\":{\"nodes\":{\"t\":{\"type\":\"mob.tick\",\"inputs\":{\"x\":{\"node\":\"t\",\"port\":\"y\"}}}}}}}}");
        assertEquals(
                List.of("/mobs/a/behaviour/nodes/t/inputs/x", "/mobs/a/behaviour/nodes/t/inputs/x"),
                new PayloadValidator(catalog)
                        .validate(payload).stream().map(PayloadProblem::path).toList());
    }

    @Test
    void routinesAreKnownByTheirDefinition() {
        String graph = """
                {"mobs":{"a":{"id":"a","attributes":{"entityType":"ZOMBIE"},"behaviour":{"nodes":{
                  "define":{"type":"routine.define","config":{"name":"jump"}},
                  "height":{"type":"routine.parameter.number","config":{"name":"height","routine":"jump"}},
                  "five":{"type":"value.number"},
                  "use":{"type":"routine.jump","inputs":{"height":{"node":"five","port":"value"}}},
                  "wrong":{"type":"routine.jump","inputs":{"width":{"node":"five","port":"value"}}},
                  "missing":{"type":"routine.fly"}}}}}}
                """;
        var problems = new PayloadValidator(Catalogs.small())
                .validate(EditorApp.MAPPER.readTree(graph)).stream()
                        .map(PayloadProblem::path)
                        .filter(path -> !path.contains("/define") && !path.contains("/height"))
                        .sorted()
                        .toList();
        assertEquals(
                List.of("/mobs/a/behaviour/nodes/missing/type", "/mobs/a/behaviour/nodes/wrong/inputs/width"),
                problems);
    }
}
