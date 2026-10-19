/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.geometry;

import de.eldoria.bloodnight.editor.payload.EditorPayload;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Writes the points of a set of shapes to a fixture the editor's preview is tested against, so the server and the
 * preview draw shapes alike. Delete the fixture and run this test to write it anew after a change to the shapes.
 */
class ShapeFixtureTest {
    private static final Path FIXTURE = Path.of("../../web/frontend/src/util/particles/shapes.fixture.json");

    private static Shape shape(String json) {
        return EditorPayload.MAPPER.readValue(json.replace('\'', '"'), Shape.class);
    }

    private static ArrayNode fixture() {
        ArrayNode cases = EditorPayload.MAPPER.createArrayNode();
        Frame east = Frame.of(new Vec3(1, 2, 3), new Vec3(1, 0.5, 0));
        Moment moment = new Moment(0.4, 3, 7);
        for (String json : List.of(
                "{'type':'POINT'}",
                "{'type':'RING','radius':1.5,'points':12,'spin':20,'facing':'UP'}",
                "{'type':'RING','radius':0.6,'points':8,'facing':'PATH','grow':true}",
                "{'type':'SPHERE','radius':0.9,'points':20}",
                "{'type':'LINE','to':{'x':1,'y':2,'z':3},'points':5,'grow':true}",
                "{'type':'SPIRAL','radius':0.7,'height':2,'turns':1.5,'points':10,'strands':2,'spin':12}",
                "{'type':'PATTERN','rows':['..xx','.xxx','xx..'],'scale':0.2,'mirror':true,"
                        + "'flap':{'degrees':25,'ticks':16},'facing':'BODY'}",
                "{'type':'PATTERN','rows':['.x.','x.x','.x.'],'scale':0.3,'spin':6,'facing':'UP'}")) {
            ObjectNode entry = cases.addObject();
            entry.put("kind", "around");
            entry.set("shape", EditorPayload.MAPPER.readTree(json.replace('\'', '"')));
            points(entry, Shapes.around(shape(json), east, moment));
        }
        for (String json : List.of(
                "{'type':'BEAM','density':3}",
                "{'type':'ARC','density':2,'bend':1.2,'twist':60,'spin':12,'grow':true}")) {
            ObjectNode entry = cases.addObject();
            entry.put("kind", "between");
            entry.set("shape", EditorPayload.MAPPER.readTree(json.replace('\'', '"')));
            points(entry, Shapes.between(shape(json), new Vec3(0, 1, 0), new Vec3(5, 3, -2), moment, new Random(1)));
        }
        return cases;
    }

    private static void points(ObjectNode entry, List<Vec3> points) {
        ArrayNode list = entry.putArray("points");
        for (Vec3 point : points) {
            list.addArray().add(round(point.x())).add(round(point.y())).add(round(point.z()));
        }
    }

    private static double round(double value) {
        return Math.round(value * 1e6) / 1e6;
    }

    @Test
    void theCommittedFixtureMatchesTheShapes() throws IOException {
        String current = EditorPayload.MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(fixture()) + "\n";
        if (!Files.exists(FIXTURE)) {
            Files.writeString(FIXTURE, current);
            fail("Wrote " + FIXTURE + "; run the test again");
        }
        assertEquals(current, Files.readString(FIXTURE), "The shapes changed: delete " + FIXTURE + " and run again");
    }
}
