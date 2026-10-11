/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.service;

import de.eldoria.bloodnight.editor.api.CreateSession;
import de.eldoria.bloodnight.editor.api.EditorRoutes;
import de.eldoria.bloodnight.editor.api.PayloadProblem;
import de.eldoria.bloodnight.editor.api.SessionCreated;
import de.eldoria.bloodnight.editor.api.SessionState;
import de.eldoria.bloodnight.editor.api.SessionView;
import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.payload.CatalogGenerator;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.TooManyRequestsResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.json.JavalinJackson3;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Duration;
import java.util.List;

/**
 * The editor service's HTTP API, see {@link EditorRoutes}.
 * <p>
 * Whoever knows a session's id can read and edit it; pulling the result and closing the session need its pull
 * token. Each address may open {@link ServiceConfig#sessionsPerHour()} sessions per hour. Payloads are checked by
 * {@link PayloadValidator} on every save; problems are reported, never refused, since the editor saves work in
 * progress.
 */
final class EditorApp {
    static final JsonMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    private static final TypeReference<List<PayloadProblem>> PROBLEMS = new TypeReference<>() {};
    private final ServiceConfig config;
    private final SessionRepository sessions;
    private final CatalogRepository catalogs;
    private final RateLimiter limiter;

    EditorApp(ServiceConfig config, SessionRepository sessions, CatalogRepository catalogs, Clock clock) {
        this.config = config;
        this.sessions = sessions;
        this.catalogs = catalogs;
        this.limiter = new RateLimiter(config.sessionsPerHour(), Duration.ofHours(1), clock);
    }

    /**
     * @return the app, not started
     */
    Javalin create() {
        return Javalin.create(javalin -> {
            javalin.jsonMapper(new JavalinJackson3(MAPPER, false));
            javalin.http.maxRequestSize = config.maxPayloadBytes();
            javalin.bundledPlugins.enableCors(cors -> cors.addRule(rule -> {
                if (config.allowedOrigins().isEmpty()) rule.anyHost();
                else config.allowedOrigins().forEach(rule::allowHost);
            }));
            javalin.routes.head(EditorRoutes.CATALOG, this::hasCatalog);
            javalin.routes.put(EditorRoutes.CATALOG, this::putCatalog);
            javalin.routes.post(EditorRoutes.SESSIONS, this::open);
            javalin.routes.get(EditorRoutes.SESSION, this::view);
            javalin.routes.put(EditorRoutes.PAYLOAD, this::save);
            javalin.routes.post(EditorRoutes.SUBMIT, this::submit);
            javalin.routes.get(EditorRoutes.RESULT, this::result);
            javalin.routes.delete(EditorRoutes.SESSION, this::close);
        });
    }

    private void hasCatalog(Context ctx) {
        ctx.status(catalogs.exists(ctx.pathParam("version")) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    private void putCatalog(Context ctx) {
        String version = ctx.pathParam("version");
        Catalog catalog;
        try {
            catalog = CatalogGenerator.read(ctx.body());
        } catch (RuntimeException e) {
            throw new BadRequestResponse("Not a catalog: " + e.getMessage());
        }
        if (!catalog.version().equals(version))
            throw new BadRequestResponse("The catalog is version " + catalog.version());
        catalogs.store(version, ctx.body());
        ctx.status(HttpStatus.NO_CONTENT);
    }

    private void open(Context ctx) {
        if (!limiter.tryAcquire(ctx.ip()))
            throw new TooManyRequestsResponse("Too many sessions opened, try again later");
        CreateSession request = ctx.bodyAsClass(CreateSession.class);
        if (request.payload() == null || !request.payload().isObject()) throw new BadRequestResponse("Missing payload");
        Catalog catalog = catalogs.find(request.catalog())
                .orElseThrow(
                        () -> new ConflictResponse("Unknown catalog %s, upload it first".formatted(request.catalog())));
        String id = Tokens.sessionId();
        String token = Tokens.pullToken();
        sessions.create(
                id,
                Tokens.hash(token),
                request.catalog(),
                MAPPER.writeValueAsString(request.payload()),
                MAPPER.writeValueAsString(new PayloadValidator(catalog).validate(request.payload())));
        StoredSession stored = sessions.find(id).orElseThrow();
        ctx.status(HttpStatus.CREATED)
                .json(new SessionCreated(id, config.editorUrl().replace("{id}", id), token, stored.expires()));
    }

    private void view(Context ctx) {
        ctx.json(view(session(ctx)));
    }

    private void save(Context ctx) {
        StoredSession session = session(ctx);
        JsonNode payload = MAPPER.readTree(ctx.body());
        List<PayloadProblem> problems = catalogs.find(session.catalog())
                .map(catalog -> new PayloadValidator(catalog).validate(payload))
                .orElse(List.of());
        if (!sessions.save(session.id(), MAPPER.writeValueAsString(payload), MAPPER.writeValueAsString(problems))) {
            throw new NotFoundResponse("No such session");
        }
        ctx.json(problems);
    }

    private void submit(Context ctx) {
        if (!sessions.submit(session(ctx).id())) throw new NotFoundResponse("No such session");
        ctx.status(HttpStatus.NO_CONTENT);
    }

    private void result(Context ctx) {
        StoredSession session = authorized(ctx);
        if (session.state() != SessionState.SUBMITTED) throw new ConflictResponse("The session is not submitted yet");
        ctx.json(view(session));
    }

    private void close(Context ctx) {
        sessions.delete(authorized(ctx).id());
        ctx.status(HttpStatus.NO_CONTENT);
    }

    private StoredSession session(Context ctx) {
        return sessions.find(ctx.pathParam("id")).orElseThrow(() -> new NotFoundResponse("No such session"));
    }

    private StoredSession authorized(Context ctx) {
        StoredSession session = session(ctx);
        String header = ctx.header("Authorization");
        if (header == null
                || !header.startsWith("Bearer ")
                || !Tokens.matches(header.substring(7), session.pullTokenHash())) {
            throw new UnauthorizedResponse("Wrong pull token");
        }
        return session;
    }

    private static SessionView view(StoredSession session) {
        return new SessionView(
                session.id(),
                session.state(),
                session.catalog(),
                MAPPER.readTree(session.payload()),
                MAPPER.readValue(session.problems(), PROBLEMS),
                session.expires());
    }
}
