/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.editor.payload.EditorPayload;
import de.eldoria.bloodnight.schema.JsonSchemas;
import de.eldoria.graph.payload.Catalog;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.node.ObjectNode;

import java.nio.file.Path;

/**
 * The JSON Schema of the editor payload the web frontend reads its payload types and settings forms from: the payload
 * with every definition it uses, plus the catalog, written as {@link EditorPayload#MAPPER} writes them.
 */
final class PayloadSchema {
    /**
     * Where the schema is committed, relative to the repository root.
     */
    static final Path PATH = Path.of("web/frontend/src/api/generated/payload.schema.json");

    private PayloadSchema() {}

    /**
     * @return the schema as written to {@link #PATH}
     */
    static String generate() {
        JsonSchemas schemas = new JsonSchemas("#/$defs/", EditorPayload.MAPPER);
        ObjectNode document = EditorPayload.MAPPER.createObjectNode();
        document.put("$schema", "https://json-schema.org/draft/2020-12/schema");
        document.put("title", "EditorPayload");
        document.setAll(schemas.schema(EditorPayload.class));
        schemas.schema(Catalog.class);
        document.set("$defs", schemas.definitions());
        return EditorPayload.MAPPER.writer(SerializationFeature.INDENT_OUTPUT).writeValueAsString(document) + "\n";
    }
}
