/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.configuration.elements.EditorSettings;
import de.eldoria.bloodnight.web.api.editor.EditorArea;
import de.eldoria.bloodnight.web.api.editor.EditorRoutes;
import de.eldoria.bloodnight.web.api.editor.SessionView;
import de.eldoria.bloodnight.web.api.editor.change.AppliedReport;
import de.eldoria.bloodnight.web.api.editor.session.AddParticipant;
import de.eldoria.bloodnight.web.api.editor.session.CreateSession;
import de.eldoria.bloodnight.web.api.editor.session.ParticipantCreated;
import de.eldoria.bloodnight.web.api.editor.session.SessionCreated;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Talks to the editor service, see {@link EditorRoutes}. Every call runs asynchronously; failures complete the future
 * with an {@link EditorException}.
 */
public final class EditorClient {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private final HttpClient http;
    private final Supplier<EditorSettings> settings;

    /**
     * @param http     sends the requests
     * @param settings where the service runs
     */
    public EditorClient(HttpClient http, Supplier<EditorSettings> settings) {
        this.http = http;
        this.settings = settings;
    }

    /**
     * Uploads the catalog unless the service has it, then opens a session.
     *
     * @param catalogVersion the catalog's version
     * @param catalog        the catalog, JSON
     * @param payload        the editor payload, JSON
     * @param participant    the name of who opens it
     * @param areas          what the opener may change
     * @return the opened session, with the opener's link
     */
    public CompletableFuture<SessionCreated> open(
            String catalogVersion, String catalog, String payload, String participant, Set<EditorArea> areas) {
        String catalogPath = EditorRoutes.path(EditorRoutes.CATALOG, catalogVersion);
        return send(request(catalogPath).method("HEAD", HttpRequest.BodyPublishers.noBody()))
                .thenCompose(head -> head.statusCode() == 200
                        ? CompletableFuture.completedFuture(head)
                        : send(request(catalogPath).PUT(HttpRequest.BodyPublishers.ofString(catalog)))
                                .thenApply(EditorClient::expectSuccess))
                .thenCompose(ignored -> {
                    String body = MAPPER.writeValueAsString(
                            new CreateSession(catalogVersion, MAPPER.readTree(payload), participant, areas));
                    return send(request(EditorRoutes.SESSIONS).POST(HttpRequest.BodyPublishers.ofString(body)));
                })
                .thenApply(response -> MAPPER.readValue(expectSuccess(response).body(), SessionCreated.class));
    }

    /**
     * @param session a session the server opened
     * @return the session as the editor sees it; empty when it expired or was closed
     */
    public CompletableFuture<Optional<SessionView>> view(SessionCreated session) {
        return send(request(EditorRoutes.path(EditorRoutes.SESSION, session.id()))
                        .GET())
                .thenApply(response -> response.statusCode() == 404
                        ? Optional.empty()
                        : Optional.of(MAPPER.readValue(expectSuccess(response).body(), SessionView.class)));
    }

    /**
     * Lets someone else edit an open session.
     *
     * @param session the session
     * @param name    their name
     * @param areas   what they may change
     * @return their personal link; empty when the session expired or was closed
     */
    public CompletableFuture<Optional<ParticipantCreated>> addParticipant(
            SessionCreated session, String name, Set<EditorArea> areas) {
        String body = MAPPER.writeValueAsString(new AddParticipant(name, areas));
        return send(authorized(EditorRoutes.path(EditorRoutes.PARTICIPANTS, session.id()), session)
                        .POST(HttpRequest.BodyPublishers.ofString(body)))
                .thenApply(response -> response.statusCode() == 404
                        ? Optional.empty()
                        : Optional.of(MAPPER.readValue(expectSuccess(response).body(), ParticipantCreated.class)));
    }

    /**
     * @param session an open session
     * @return the session as submitted in the editor
     */
    public CompletableFuture<SessionView> pull(SessionCreated session) {
        return send(authorized(EditorRoutes.path(EditorRoutes.RESULT, session.id()), session)
                        .GET())
                .thenApply(response -> {
                    if (response.statusCode() == 409) {
                        throw new EditorException("The session is not marked as done in the editor yet");
                    }
                    if (response.statusCode() == 404) throw new EditorException("The session expired or was closed");
                    return MAPPER.readValue(expectSuccess(response).body(), SessionView.class);
                });
    }

    /**
     * Tells the service that the server applied the session and keeps it open.
     *
     * @param session  the session
     * @param revision the revision the server applied
     * @param hashes   the server's file hashes after the apply, which the next apply is checked against
     * @return completes once the service recorded it
     */
    public CompletableFuture<Void> applied(SessionCreated session, int revision, Map<String, String> hashes) {
        String body = MAPPER.writeValueAsString(new AppliedReport(revision, hashes));
        return send(authorized(EditorRoutes.path(EditorRoutes.APPLIED, session.id()), session)
                        .POST(HttpRequest.BodyPublishers.ofString(body)))
                .thenAccept(response -> {
                    if (response.statusCode() == 404) throw new EditorException("The session expired or was closed");
                    expectSuccess(response);
                });
    }

    /**
     * @param session an open session
     * @return completes once the service deleted it
     */
    public CompletableFuture<Void> close(SessionCreated session) {
        return send(authorized(EditorRoutes.path(EditorRoutes.SESSION, session.id()), session)
                        .DELETE())
                .thenAccept(response -> {
                    if (response.statusCode() != 404) expectSuccess(response);
                });
    }

    private HttpRequest.Builder request(String path) {
        EditorSettings editor = settings.get();
        return HttpRequest.newBuilder(URI.create(editor.url() + path))
                .timeout(Duration.ofSeconds(editor.timeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    private HttpRequest.Builder authorized(String path, SessionCreated session) {
        return request(path).header("Authorization", "Bearer " + session.pullToken());
    }

    private CompletableFuture<HttpResponse<String>> send(HttpRequest.Builder request) {
        return http.sendAsync(request.build(), HttpResponse.BodyHandlers.ofString())
                .exceptionally(error -> {
                    throw new EditorException("The editor service at %s cannot be reached: %s"
                            .formatted(settings.get().url(), error.getMessage()));
                });
    }

    private static HttpResponse<String> expectSuccess(HttpResponse<String> response) {
        if (response.statusCode() / 100 == 2) return response;
        throw new EditorException(
                "The editor service answered %d: %s".formatted(response.statusCode(), response.body()));
    }
}
