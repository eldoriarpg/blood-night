/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.schema.JsonSchemas;
import de.eldoria.bloodnight.web.api.assets.AssetRoutes;
import de.eldoria.bloodnight.web.api.assets.ItemIndex;
import de.eldoria.bloodnight.web.api.editor.AddParticipant;
import de.eldoria.bloodnight.web.api.editor.ChangeAccepted;
import de.eldoria.bloodnight.web.api.editor.ChangeBatch;
import de.eldoria.bloodnight.web.api.editor.CreateSession;
import de.eldoria.bloodnight.web.api.editor.EditorRoutes;
import de.eldoria.bloodnight.web.api.editor.LockRequest;
import de.eldoria.bloodnight.web.api.editor.ParticipantCreated;
import de.eldoria.bloodnight.web.api.editor.RevisionInfo;
import de.eldoria.bloodnight.web.api.editor.SessionCreated;
import de.eldoria.bloodnight.web.api.editor.SessionView;
import de.eldoria.bloodnight.web.api.editor.StreamEvent;
import de.eldoria.graph.payload.Catalog;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The OpenAPI 3.1 description of the web backend's API, which the frontend generates its types from: every route of
 * {@link EditorRoutes} with its body and answers, the schemas of the records they carry, and the stream's events.
 */
final class ApiSpec {
    /**
     * Where the description is committed, relative to the repository root.
     */
    static final Path PATH = Path.of("web/backend/src/main/resources/api/openapi.json");

    private static final Pattern PARAMETER = Pattern.compile("\\{([a-z]+)}");

    /**
     * The body Javalin answers an error with.
     *
     * @param title   what went wrong
     * @param status  the status code
     * @param type    the documentation link Javalin adds
     * @param details further values, e.g. the holders of locked sections by section
     */
    record ErrorBody(
            String title, int status, String type, @Nullable Map<String, String> details) {}

    /**
     * One operation.
     *
     * @param method   the HTTP method
     * @param route    the route, see {@link EditorRoutes}
     * @param id       the operation's id
     * @param summary  what it does
     * @param auth     which bearer it takes: {@code participant}, {@code pull}, {@code either} or none
     * @param request  the body it reads, if any
     * @param status   the status of a success
     * @param response the body of a success, if any
     */
    record Operation(
            String method,
            String route,
            String id,
            String summary,
            @Nullable String auth,
            @Nullable Type request,
            int status,
            @Nullable Type response) {}

    static final List<Operation> OPERATIONS = List.of(
            new Operation(
                    "head",
                    EditorRoutes.CATALOG,
                    "hasCatalog",
                    "Whether the service has a catalog",
                    null,
                    null,
                    200,
                    null),
            new Operation(
                    "put", EditorRoutes.CATALOG, "putCatalog", "Uploads a catalog", null, Catalog.class, 204, null),
            new Operation(
                    "post",
                    EditorRoutes.SESSIONS,
                    "openSession",
                    "Opens a session",
                    null,
                    CreateSession.class,
                    201,
                    SessionCreated.class),
            new Operation(
                    "get", EditorRoutes.SESSION, "getSession", "The session", "either", null, 200, SessionView.class),
            new Operation(
                    "delete", EditorRoutes.SESSION, "closeSession", "Closes the session", "pull", null, 204, null),
            new Operation(
                    "post",
                    EditorRoutes.PARTICIPANTS,
                    "addParticipant",
                    "Lets someone else edit",
                    "pull",
                    AddParticipant.class,
                    201,
                    ParticipantCreated.class),
            new Operation(
                    "post",
                    EditorRoutes.LOCKS,
                    "takeSection",
                    "Takes a section",
                    "participant",
                    LockRequest.class,
                    204,
                    null),
            new Operation(
                    "delete",
                    EditorRoutes.LOCKS,
                    "releaseSection",
                    "Releases a section",
                    "participant",
                    LockRequest.class,
                    204,
                    null),
            new Operation(
                    "put",
                    EditorRoutes.PAYLOAD,
                    "replacePayload",
                    "Replaces the whole payload",
                    "participant",
                    JsonNode.class,
                    200,
                    ChangeAccepted.class),
            new Operation(
                    "post",
                    EditorRoutes.CHANGES,
                    "applyChanges",
                    "Applies a change",
                    "participant",
                    ChangeBatch.class,
                    200,
                    ChangeAccepted.class),
            new Operation(
                    "get",
                    EditorRoutes.REVISIONS,
                    "listRevisions",
                    "The revisions",
                    "participant",
                    null,
                    200,
                    RevisionInfo[].class),
            new Operation(
                    "get",
                    EditorRoutes.REVISION,
                    "getRevision",
                    "The payload at a revision",
                    "participant",
                    null,
                    200,
                    JsonNode.class),
            new Operation(
                    "post",
                    EditorRoutes.RESTORE,
                    "restoreRevision",
                    "Restores a revision",
                    "participant",
                    null,
                    200,
                    ChangeAccepted.class),
            new Operation(
                    "post",
                    EditorRoutes.SUBMIT,
                    "submitSession",
                    "Marks the session as done",
                    "participant",
                    null,
                    204,
                    null),
            new Operation(
                    "get",
                    EditorRoutes.RESULT,
                    "pullResult",
                    "The submitted session",
                    "pull",
                    null,
                    200,
                    SessionView.class),
            new Operation(
                    "get",
                    AssetRoutes.ITEMS,
                    "getItemAssets",
                    "Every item of a Minecraft version with its icon and names",
                    null,
                    null,
                    200,
                    ItemIndex.class),
            new Operation("get", AssetRoutes.TEXTURE, "getItemTexture", "An item icon as PNG", null, null, 200, null));

    private ApiSpec() {}

    /**
     * @return the description as written to {@link #PATH}
     */
    static String generate() {
        JsonSchemas schemas = new JsonSchemas("#/components/schemas/", EditorApp.MAPPER);
        ObjectNode spec = EditorApp.MAPPER.createObjectNode();
        spec.put("openapi", "3.1.0");
        spec.putObject("info").put("title", "BloodNight web").put("version", "1");
        ObjectNode paths = spec.putObject("paths");
        for (Operation operation : OPERATIONS) {
            ObjectNode path = paths.has(operation.route())
                    ? (ObjectNode) paths.get(operation.route())
                    : paths.putObject(operation.route());
            path.set(operation.method(), operation(operation, schemas));
        }
        schemas.schema(StreamEvent.class);
        schemas.schema(ErrorBody.class);
        ObjectNode components = spec.putObject("components");
        components.set("schemas", schemas.definitions());
        components
                .putObject("securitySchemes")
                .putObject("bearer")
                .put("type", "http")
                .put("scheme", "bearer")
                .put("description", "A participant token, or the server's pull token, as the operation says");
        return EditorApp.MAPPER.writer(SerializationFeature.INDENT_OUTPUT).writeValueAsString(spec) + "\n";
    }

    private static ObjectNode operation(Operation operation, JsonSchemas schemas) {
        ObjectNode node = EditorApp.MAPPER.createObjectNode();
        node.put("operationId", operation.id());
        node.put("summary", operation.summary());
        ArrayNode parameters = EditorApp.MAPPER.createArrayNode();
        Matcher matcher = PARAMETER.matcher(operation.route());
        while (matcher.find()) {
            ObjectNode parameter = parameters
                    .addObject()
                    .put("name", matcher.group(1))
                    .put("in", "path")
                    .put("required", true);
            parameter.putObject("schema").put("type", matcher.group(1).equals("revision") ? "integer" : "string");
        }
        if (!parameters.isEmpty()) node.set("parameters", parameters);
        if (operation.auth() != null) {
            node.putArray("security").addObject().putArray("bearer");
            node.put("x-bearer", operation.auth());
        }
        if (operation.request() != null) {
            node.putObject("requestBody")
                    .put("required", true)
                    .putObject("content")
                    .putObject("application/json")
                    .set("schema", schemas.schema(operation.request()));
        }
        ObjectNode responses = node.putObject("responses");
        ObjectNode success =
                responses.putObject(Integer.toString(operation.status())).put("description", "Success");
        if (operation.response() != null) {
            success.putObject("content")
                    .putObject("application/json")
                    .set("schema", schemas.schema(operation.response()));
        }
        ObjectNode error = responses.putObject("default").put("description", "An error, e.g. 404, 409 or 423 Locked");
        error.putObject("content").putObject("application/json").set("schema", schemas.schema(ErrorBody.class));
        return node;
    }
}
