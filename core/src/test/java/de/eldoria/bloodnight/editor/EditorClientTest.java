/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.sun.net.httpserver.HttpServer;
import de.eldoria.bloodnight.configuration.elements.EditorSettings;
import de.eldoria.bloodnight.editor.api.SessionCreated;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EditorClientTest {
    private static final SessionCreated SESSION =
            new SessionCreated("id", "https://editor.test/session/id", "token", Instant.parse("2026-10-12T00:00:00Z"));
    private final List<String> requests = new ArrayList<>();
    private HttpServer server;
    private EditorClient client;
    private int resultStatus = 409;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            requests.add(exchange.getRequestMethod() + " " + path + (auth == null ? "" : " " + auth));
            int status;
            String body = "";
            if (path.startsWith("/api/v1/catalogs/")) {
                status = exchange.getRequestMethod().equals("HEAD") ? 404 : 204;
            } else if (path.equals("/api/v1/sessions")) {
                status = 201;
                body = JsonMapper.builder().build().writeValueAsString(SESSION);
            } else if (path.endsWith("/result")) {
                status = resultStatus;
            } else {
                status = 204;
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        EditorSettings settings = JsonMapper.builder()
                .changeDefaultVisibility(v -> v.withFieldVisibility(JsonAutoDetect.Visibility.ANY))
                .build()
                .readValue(
                        "{\"url\":\"http://localhost:%d/\"}"
                                .formatted(server.getAddress().getPort()),
                        EditorSettings.class);
        client = new EditorClient(HttpClient.newHttpClient(), () -> settings);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void openingUploadsAMissingCatalogFirst() {
        SessionCreated session = client.open("v1", "{}", "{\"mobs\":{}}").join();
        assertEquals(SESSION, session);
        assertEquals(List.of("HEAD /api/v1/catalogs/v1", "PUT /api/v1/catalogs/v1", "POST /api/v1/sessions"), requests);
    }

    @Test
    void pullingAnUnsubmittedSessionSaysSo() {
        var error = assertThrows(
                CompletionException.class, () -> client.pull(SESSION).join());
        assertInstanceOf(EditorException.class, error.getCause());
        assertTrue(error.getCause().getMessage().contains("not marked as done"));
        assertEquals(List.of("GET /api/v1/sessions/id/result Bearer token"), requests);
    }

    @Test
    void anUnreachableServiceIsReported() {
        server.stop(0);
        var error = assertThrows(
                CompletionException.class, () -> client.close(SESSION).join());
        assertTrue(error.getCause().getMessage().contains("cannot be reached"));
    }

    @Test
    void theSessionIsKeptInAFile(@TempDir Path folder) {
        EditorSessionFile file = new EditorSessionFile(folder.resolve("editor-session.json"));
        assertTrue(file.load().isEmpty());
        file.save(SESSION);
        assertEquals(SESSION, file.load().orElseThrow());
        file.clear();
        assertTrue(file.load().isEmpty());
    }
}
