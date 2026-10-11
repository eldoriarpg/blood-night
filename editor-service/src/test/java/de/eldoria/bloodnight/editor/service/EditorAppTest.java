/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.service;

import de.eldoria.bloodnight.editor.api.EditorRoutes;
import de.eldoria.bloodnight.editor.api.SessionCreated;
import de.eldoria.bloodnight.editor.api.SessionState;
import de.eldoria.bloodnight.editor.api.SessionView;
import de.eldoria.graph.payload.CatalogGenerator;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorAppTest {
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");
    private static final String PAYLOAD =
            "{\"mobs\":{\"hot\":{\"id\":\"hot\",\"attributes\":{\"entityType\":\"ZOMBIE\"}}}}";
    private static Javalin app;
    private static String base;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void start() throws SQLException, IOException {
        POSTGRES.start();
        var database = new ServiceConfig.DatabaseConfig(
                POSTGRES.getHost(),
                POSTGRES.getFirstMappedPort(),
                POSTGRES.getDatabaseName(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword(),
                "editor");
        ServiceConfig config = new ServiceConfig(
                0, "https://editor.test/session/{id}", List.of(), Duration.ofHours(1), 3, 1024 * 1024, database);
        Database.open(database);
        app = new EditorApp(
                        config,
                        new SessionRepository(config.sessionLifetime()),
                        new CatalogRepository(),
                        Clock.systemUTC())
                .create()
                .start(0);
        base = "http://localhost:" + app.port();
    }

    @AfterAll
    static void stop() {
        app.stop();
        POSTGRES.stop();
    }

    private HttpResponse<String> send(String method, String path, String body, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + path))
                .method(
                        method,
                        body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void uploadCatalog() throws Exception {
        send(
                "PUT",
                EditorRoutes.path(EditorRoutes.CATALOG, "test-catalog"),
                CatalogGenerator.write(Catalogs.small()),
                null);
    }

    private SessionCreated open() throws Exception {
        var response =
                send("POST", EditorRoutes.SESSIONS, "{\"catalog\":\"test-catalog\",\"payload\":" + PAYLOAD + "}", null);
        assertEquals(201, response.statusCode(), response.body());
        return EditorApp.MAPPER.readValue(response.body(), SessionCreated.class);
    }

    @Test
    void catalogsAreUploadedOnce() throws Exception {
        assertEquals(
                404,
                send("HEAD", EditorRoutes.path(EditorRoutes.CATALOG, "other"), null, null)
                        .statusCode());
        uploadCatalog();
        assertEquals(
                200,
                send("HEAD", EditorRoutes.path(EditorRoutes.CATALOG, "test-catalog"), null, null)
                        .statusCode());
        assertEquals(
                400,
                send(
                                "PUT",
                                EditorRoutes.path(EditorRoutes.CATALOG, "other"),
                                CatalogGenerator.write(Catalogs.small()),
                                null)
                        .statusCode());
    }

    @Test
    void aSessionGoesFromServerToEditorAndBack() throws Exception {
        uploadCatalog();
        SessionCreated session = open();
        assertEquals("https://editor.test/session/" + session.id(), session.editorUrl());
        String path = EditorRoutes.path(EditorRoutes.SESSION, session.id());

        SessionView view =
                EditorApp.MAPPER.readValue(send("GET", path, null, null).body(), SessionView.class);
        assertEquals(SessionState.EDITING, view.state());
        assertEquals(
                "ZOMBIE",
                view.payload()
                        .path("mobs")
                        .path("hot")
                        .path("attributes")
                        .path("entityType")
                        .asString());

        String edited = PAYLOAD.replace("ZOMBIE", "HUSK")
                .replace("}}}}", "},\"behaviour\":{\"nodes\":{\"x\":{\"type\":\"nope\"}}}}}}");
        var saved = send("PUT", EditorRoutes.path(EditorRoutes.PAYLOAD, session.id()), edited, null);
        assertTrue(saved.body().contains("Unknown node type nope"), saved.body());

        String result = EditorRoutes.path(EditorRoutes.RESULT, session.id());
        assertEquals(409, send("GET", result, null, session.pullToken()).statusCode());
        assertEquals(
                204,
                send("POST", EditorRoutes.path(EditorRoutes.SUBMIT, session.id()), null, null)
                        .statusCode());
        assertEquals(401, send("GET", result, null, "wrong").statusCode());
        SessionView pulled = EditorApp.MAPPER.readValue(
                send("GET", result, null, session.pullToken()).body(), SessionView.class);
        assertEquals(SessionState.SUBMITTED, pulled.state());
        assertEquals(
                "HUSK",
                pulled.payload()
                        .path("mobs")
                        .path("hot")
                        .path("attributes")
                        .path("entityType")
                        .asString());
        assertEquals(1, pulled.problems().size());

        assertEquals(401, send("DELETE", path, null, null).statusCode());
        assertEquals(204, send("DELETE", path, null, session.pullToken()).statusCode());
        assertEquals(404, send("GET", path, null, null).statusCode());
    }

    @Test
    void sessionsNeedAKnownCatalog() throws Exception {
        var response = send("POST", EditorRoutes.SESSIONS, "{\"catalog\":\"missing\",\"payload\":{}}", null);
        assertEquals(409, response.statusCode());
    }
}
