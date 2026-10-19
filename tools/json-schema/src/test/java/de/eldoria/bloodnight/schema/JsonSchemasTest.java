/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.schema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSchemasTest {
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultVisibility(v -> v.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE))
            .build();

    enum Mode {
        NORMAL,
        STRETCHED
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    record Sparse(String id, List<String> tags) {}

    record PartlySparse(
            String id,
            @JsonInclude(JsonInclude.Include.NON_EMPTY) List<String> tags) {}

    record Mob(String id, @Nullable String name, List<String> tags, Map<String, Integer> weights, JsonNode editor) {}

    static class Night {
        private int begin = 13000;
        private Mode mode = Mode.STRETCHED;
        private Set<Mode> allowed = EnumSet.of(Mode.NORMAL);

        @MaxItems(3)
        private List<String> commands = new ArrayList<>(List.of("say hi"));

        private Inner inner = new Inner();

        @JsonProperty("skip")
        private boolean skippable;

        @JsonIgnore
        private int cache;

        private static final int CONSTANT = 1;
        private transient int scratch;
    }

    static class Inner {
        private Instant at;
    }

    static class Base {
        private String base = "b";
    }

    static class Child extends Base {
        private String child = "c";
    }

    private static JsonNode definition(ObjectNode document, String name) {
        return document.path("$defs").path(name);
    }

    @Test
    void recordsListTheirComponentsAndWhatIsRequired() {
        ObjectNode document = JsonSchemas.document(Mob.class, MAPPER);
        assertEquals("#/$defs/Mob", document.path("$ref").asString());
        JsonNode mob = definition(document, "Mob");
        assertEquals("string", mob.at("/properties/id/type").asString());
        assertEquals("array", mob.at("/properties/tags/type").asString());
        assertEquals(
                "integer",
                mob.at("/properties/weights/additionalProperties/type").asString());
        assertTrue(mob.at("/properties/editor").isEmpty());
        List<String> required = new ArrayList<>();
        mob.path("required").forEach(node -> required.add(node.asString()));
        assertEquals(List.of("id", "tags", "weights", "editor"), required);
    }

    @Test
    void recordsLeavingValuesOutRequireNothing() {
        assertFalse(
                definition(JsonSchemas.document(Sparse.class, MAPPER), "Sparse").has("required"));
    }

    @Test
    void componentsLeftOutWhenEmptyAreOptional() {
        JsonNode schema = definition(JsonSchemas.document(PartlySparse.class, MAPPER), "PartlySparse");
        assertEquals("[\"id\"]", schema.get("required").toString());
    }

    @Test
    void classesListTheirFieldsWithDefaults() {
        ObjectNode document = JsonSchemas.document(Night.class, MAPPER);
        JsonNode night = definition(document, "Night");
        assertEquals(13000, night.at("/properties/begin/default").asInt());
        assertEquals("STRETCHED", night.at("/properties/mode/default").asString());
        assertEquals("#/$defs/Mode", night.at("/properties/mode/allOf/0/$ref").asString());
        assertEquals("NORMAL", definition(document, "Mode").at("/enum/0").asString());
        assertTrue(night.at("/properties/allowed/uniqueItems").asBoolean());
        assertEquals("say hi", night.at("/properties/commands/default/0").asString());
        assertEquals(3, night.at("/properties/commands/maxItems").asInt());
        assertEquals("#/$defs/Inner", night.at("/properties/inner/allOf/0/$ref").asString());
        assertTrue(night.at("/properties/skip").isObject());
        assertFalse(night.path("properties").has("cache"));
        assertFalse(night.path("properties").has("CONSTANT"));
        assertFalse(night.path("properties").has("scratch"));
        assertFalse(night.has("required"));
        assertEquals(
                "date-time",
                definition(document, "Inner").at("/properties/at/format").asString());
    }

    @Test
    void inheritedFieldsComeFirst() {
        JsonNode child = definition(JsonSchemas.document(Child.class, MAPPER), "Child");
        List<String> names = new ArrayList<>();
        child.path("properties").propertyNames().forEach(names::add);
        assertEquals(List.of("base", "child"), names);
    }
}
