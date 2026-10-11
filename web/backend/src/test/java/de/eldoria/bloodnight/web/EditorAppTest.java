/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.web.api.editor.ChangeAccepted;
import de.eldoria.bloodnight.web.api.editor.EditorRoutes;
import de.eldoria.bloodnight.web.api.editor.ParticipantCreated;
import de.eldoria.bloodnight.web.api.editor.RevisionInfo;
import de.eldoria.bloodnight.web.api.editor.SessionCreated;
import de.eldoria.bloodnight.web.api.editor.SessionState;
import de.eldoria.bloodnight.web.api.editor.SessionView;
import de.eldoria.bloodnight.web.api.editor.StreamEvent;
import de.eldoria.bloodnight.web.api.editor.StreamEventType;
import de.eldoria.graph.payload.CatalogGenerator;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorAppTest {
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");
    private static final String PAYLOAD = """
            {"mobs":{"hot":{"id":"hot","attributes":{"entityType":"ZOMBIE"}},
                     "cold":{"id":"cold","attributes":{"entityType":"STRAY"}}},
             "settings":{"main":{"prefix":"a"}}}""";
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
                0,
                "https://editor.test/session/{id}#{token}",
                List.of(),
                Duration.ofHours(1),
                20,
                1024 * 1024,
                database);
        Database.open(database);
        app = new EditorApp(
                        config,
                        new SessionRepository(config.sessionLifetime()),
                        new ParticipantRepository(),
                        new CatalogRepository(),
                        new Rooms(Clock.systemUTC(), Duration.ofMinutes(2)),
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
                        body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
                .header("Accept", "application/json");
        if (token != null) request.header("Authorization", "Bearer " + token);
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String token(String editorUrl) {
        return editorUrl.substring(editorUrl.indexOf('#') + 1);
    }

    private SessionCreated open(String name) throws Exception {
        send(
                "PUT",
                EditorRoutes.path(EditorRoutes.CATALOG, "test-catalog"),
                CatalogGenerator.write(Catalogs.small()),
                null);
        String body = "{\"catalog\":\"test-catalog\",\"participant\":\"%s\",\"payload\":%s}".formatted(name, PAYLOAD);
        var response = send("POST", EditorRoutes.SESSIONS, body, null);
        assertEquals(201, response.statusCode(), response.body());
        return EditorApp.MAPPER.readValue(response.body(), SessionCreated.class);
    }

    private String join(SessionCreated session, String name) throws Exception {
        var response = send(
                "POST",
                EditorRoutes.path(EditorRoutes.PARTICIPANTS, session.id()),
                "{\"name\":\"%s\"}".formatted(name),
                session.pullToken());
        assertEquals(201, response.statusCode(), response.body());
        return token(EditorApp.MAPPER
                .readValue(response.body(), ParticipantCreated.class)
                .editorUrl());
    }

    private HttpResponse<String> change(SessionCreated session, String token, String operations) throws Exception {
        return send(
                "POST",
                EditorRoutes.path(EditorRoutes.CHANGES, session.id()),
                "{\"operations\":" + operations + "}",
                token);
    }

    private static String entityType(String mob, String type) {
        return "[{\"op\":\"replace\",\"path\":\"/mobs/%s/attributes/entityType\",\"value\":\"%s\"}]"
                .formatted(mob, type);
    }

    private SessionView view(SessionCreated session, String token) throws Exception {
        var response = send("GET", EditorRoutes.path(EditorRoutes.SESSION, session.id()), null, token);
        assertEquals(200, response.statusCode(), response.body());
        return EditorApp.MAPPER.readValue(response.body(), SessionView.class);
    }

    @Test
    void catalogsAreUploadedOnce() throws Exception {
        assertEquals(
                404,
                send("HEAD", EditorRoutes.path(EditorRoutes.CATALOG, "other"), null, null)
                        .statusCode());
        send(
                "PUT",
                EditorRoutes.path(EditorRoutes.CATALOG, "test-catalog"),
                CatalogGenerator.write(Catalogs.small()),
                null);
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
    void sessionsNeedAKnownCatalogAndAName() throws Exception {
        assertEquals(
                409,
                send(
                                "POST",
                                EditorRoutes.SESSIONS,
                                "{\"catalog\":\"missing\",\"participant\":\"x\",\"payload\":{}}",
                                null)
                        .statusCode());
        send(
                "PUT",
                EditorRoutes.path(EditorRoutes.CATALOG, "test-catalog"),
                CatalogGenerator.write(Catalogs.small()),
                null);
        assertEquals(
                400,
                send(
                                "POST",
                                EditorRoutes.SESSIONS,
                                "{\"catalog\":\"test-catalog\",\"participant\":\" \",\"payload\":{}}",
                                null)
                        .statusCode());
    }

    @Test
    void aSessionGoesFromServerToEditorsAndBack() throws Exception {
        SessionCreated session = open("Steve");
        assertTrue(session.editorUrl().startsWith("https://editor.test/session/" + session.id() + "#"));
        String steve = token(session.editorUrl());
        String path = EditorRoutes.path(EditorRoutes.SESSION, session.id());
        assertEquals(401, send("GET", path, null, null).statusCode());
        assertEquals(SessionState.EDITING, view(session, steve).state());
        assertEquals(
                "ZOMBIE",
                view(session, session.pullToken())
                        .payload()
                        .at("/mobs/hot/attributes/entityType")
                        .asString());

        assertEquals(200, change(session, steve, entityType("hot", "HUSK")).statusCode());
        String result = EditorRoutes.path(EditorRoutes.RESULT, session.id());
        assertEquals(409, send("GET", result, null, session.pullToken()).statusCode());
        assertEquals(
                204,
                send("POST", EditorRoutes.path(EditorRoutes.SUBMIT, session.id()), null, steve)
                        .statusCode());
        assertEquals(401, send("GET", result, null, steve).statusCode());
        SessionView pulled = EditorApp.MAPPER.readValue(
                send("GET", result, null, session.pullToken()).body(), SessionView.class);
        assertEquals(
                "HUSK", pulled.payload().at("/mobs/hot/attributes/entityType").asString());

        assertEquals(401, send("DELETE", path, null, steve).statusCode());
        assertEquals(204, send("DELETE", path, null, session.pullToken()).statusCode());
        assertEquals(404, send("GET", path, null, steve).statusCode());
    }

    @Test
    void participantsWorkOnDifferentSectionsAtOnce() throws Exception {
        SessionCreated session = open("Steve");
        String steve = token(session.editorUrl());
        String alex = join(session, "Alex");

        assertEquals(200, change(session, steve, entityType("hot", "HUSK")).statusCode());
        var blocked = change(session, alex, entityType("hot", "DROWNED"));
        assertEquals(423, blocked.statusCode());
        assertTrue(blocked.body().contains("Steve"), blocked.body());
        assertEquals(200, change(session, alex, entityType("cold", "BOGGED")).statusCode());
        assertEquals(
                423,
                send("POST", EditorRoutes.path(EditorRoutes.LOCKS, session.id()), "{\"section\":\"mobs/cold\"}", steve)
                        .statusCode());
        assertEquals(
                423,
                send("POST", EditorRoutes.path(EditorRoutes.RESTORE, session.id(), 0), null, steve)
                        .statusCode());
        assertEquals(
                400,
                change(session, steve, "[{\"op\":\"add\",\"path\":\"/items/x\",\"value\":{}}]")
                        .statusCode());

        SessionView view = view(session, steve);
        assertEquals(2, view.revision());
        assertEquals(
                "HUSK", view.payload().at("/mobs/hot/attributes/entityType").asString());
        assertEquals(
                "BOGGED", view.payload().at("/mobs/cold/attributes/entityType").asString());
        assertEquals(2, view.locks().size());

        assertEquals(
                204,
                send("DELETE", EditorRoutes.path(EditorRoutes.LOCKS, session.id()), "{\"section\":\"mobs/hot\"}", steve)
                        .statusCode());
        assertEquals(200, change(session, alex, entityType("hot", "DROWNED")).statusCode());
    }

    @Test
    void revisionsKeepTheirAuthorAndCanBeRestored() throws Exception {
        SessionCreated session = open("Steve");
        String steve = token(session.editorUrl());
        change(session, steve, entityType("hot", "HUSK"));
        change(session, steve, entityType("hot", "DROWNED"));

        List<RevisionInfo> revisions = EditorApp.MAPPER.readValue(
                send("GET", EditorRoutes.path(EditorRoutes.REVISIONS, session.id()), null, steve)
                        .body(),
                new TypeReference<List<RevisionInfo>>() {});
        assertEquals(
                List.of(0, 1, 2), revisions.stream().map(RevisionInfo::revision).toList());
        assertEquals("Steve", revisions.get(1).author());
        var first = EditorApp.MAPPER.readTree(
                send("GET", EditorRoutes.path(EditorRoutes.REVISION, session.id(), 1), null, steve)
                        .body());
        assertEquals("HUSK", first.at("/mobs/hot/attributes/entityType").asString());
        assertEquals(
                404,
                send("GET", EditorRoutes.path(EditorRoutes.REVISION, session.id(), 3), null, steve)
                        .statusCode());

        var restored = send("POST", EditorRoutes.path(EditorRoutes.RESTORE, session.id(), 1), null, steve);
        assertEquals(200, restored.statusCode(), restored.body());
        assertEquals(
                3,
                EditorApp.MAPPER
                        .readValue(restored.body(), ChangeAccepted.class)
                        .revision());
        assertEquals(
                "HUSK",
                view(session, steve)
                        .payload()
                        .at("/mobs/hot/attributes/entityType")
                        .asString());
    }

    @Test
    void theStreamTellsEveryoneWhatHappens() throws Exception {
        SessionCreated session = open("Steve");
        String steve = token(session.editorUrl());
        String alex = join(session, "Alex");
        LinkedBlockingQueue<StreamEvent> heard = new LinkedBlockingQueue<>();
        WebSocket socket = client.newWebSocketBuilder()
                .buildAsync(
                        URI.create(base.replace("http", "ws") + EditorRoutes.path(EditorRoutes.STREAM, session.id())
                                + "?token=" + alex),
                        new WebSocket.Listener() {
                            @Override
                            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                                heard.add(EditorApp.MAPPER.readValue(data.toString(), StreamEvent.class));
                                webSocket.request(1);
                                return null;
                            }
                        })
                .join();

        StreamEvent hello = heard.poll(5, TimeUnit.SECONDS);
        assertNotNull(hello);
        assertEquals(StreamEventType.HELLO, hello.type());
        assertEquals(StreamEventType.PRESENCE, heard.poll(5, TimeUnit.SECONDS).type());
        assertEquals(List.of("Alex"), view(session, steve).online());

        change(session, steve, entityType("hot", "HUSK"));
        StreamEvent lock = heard.poll(5, TimeUnit.SECONDS);
        assertEquals(StreamEventType.LOCK, lock.type());
        assertEquals("mobs/hot", lock.section());
        assertEquals("Steve", lock.holder());
        StreamEvent change = heard.poll(5, TimeUnit.SECONDS);
        assertEquals(StreamEventType.CHANGE, change.type());
        assertEquals(1, change.revision());
        assertEquals("Steve", change.author());

        send("DELETE", EditorRoutes.path(EditorRoutes.SESSION, session.id()), null, session.pullToken());
        assertEquals(StreamEventType.CLOSED, heard.poll(5, TimeUnit.SECONDS).type());
        socket.abort();
    }
}
