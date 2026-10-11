/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.web.api.editor.AddParticipant;
import de.eldoria.bloodnight.web.api.editor.ChangeAccepted;
import de.eldoria.bloodnight.web.api.editor.ChangeBatch;
import de.eldoria.bloodnight.web.api.editor.CreateSession;
import de.eldoria.bloodnight.web.api.editor.EditorRoutes;
import de.eldoria.bloodnight.web.api.editor.LockInfo;
import de.eldoria.bloodnight.web.api.editor.LockRequest;
import de.eldoria.bloodnight.web.api.editor.ParticipantCreated;
import de.eldoria.bloodnight.web.api.editor.PayloadProblem;
import de.eldoria.bloodnight.web.api.editor.RevisionInfo;
import de.eldoria.bloodnight.web.api.editor.SessionCreated;
import de.eldoria.bloodnight.web.api.editor.SessionState;
import de.eldoria.bloodnight.web.api.editor.SessionView;
import de.eldoria.bloodnight.web.api.editor.StreamEvent;
import de.eldoria.bloodnight.web.api.editor.StreamEventType;
import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.payload.CatalogGenerator;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ConflictResponse;
import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.HttpStatus;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.TooManyRequestsResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.json.JavalinJackson3;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The editor service's HTTP and WebSocket API, see {@link EditorRoutes}.
 * <p>
 * A session is shared by everyone the server lets in: each participant has a personal link whose token identifies
 * them on every editing call; the server's own calls use the pull token. Each address may open
 * {@link ServiceConfig#sessionsPerHour()} sessions per hour.
 * <p>
 * Every save is a revision: a batch of JSON Patch operations, a whole payload or a restored revision, each turned
 * into operations and kept with its author, so any revision can be looked at and restored. Changes apply on the
 * latest revision and may only touch sections that are free or held by their sender (see {@link Sections},
 * {@link Room}); touching a free section takes it. Concurrent saves are retried on top of each other, so none is
 * lost. Payloads are checked by {@link PayloadValidator} on every save; problems are reported, never refused, since
 * the editor saves work in progress. Everyone connected to a session's stream hears about changes, locks and who is
 * online.
 */
final class EditorApp {
    static final JsonMapper MAPPER = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    private static final TypeReference<List<PayloadProblem>> PROBLEMS = new TypeReference<>() {};
    private static final int SAVE_ATTEMPTS = 10;
    private static final int MAX_NAME = 32;
    private final ServiceConfig config;
    private final SessionRepository sessions;
    private final ParticipantRepository participants;
    private final CatalogRepository catalogs;
    private final Rooms rooms;
    private final RateLimiter limiter;

    EditorApp(
            ServiceConfig config,
            SessionRepository sessions,
            ParticipantRepository participants,
            CatalogRepository catalogs,
            Rooms rooms,
            Clock clock) {
        this.config = config;
        this.sessions = sessions;
        this.participants = participants;
        this.catalogs = catalogs;
        this.rooms = rooms;
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
            javalin.routes.post(EditorRoutes.PARTICIPANTS, this::addParticipant);
            javalin.routes.post(EditorRoutes.LOCKS, this::lock);
            javalin.routes.delete(EditorRoutes.LOCKS, this::unlock);
            javalin.routes.put(EditorRoutes.PAYLOAD, this::save);
            javalin.routes.post(EditorRoutes.CHANGES, this::changes);
            javalin.routes.get(EditorRoutes.REVISIONS, this::revisions);
            javalin.routes.get(EditorRoutes.REVISION, this::revision);
            javalin.routes.post(EditorRoutes.RESTORE, this::restore);
            javalin.routes.post(EditorRoutes.SUBMIT, this::submit);
            javalin.routes.get(EditorRoutes.RESULT, this::result);
            javalin.routes.delete(EditorRoutes.SESSION, this::close);
            javalin.routes.ws(EditorRoutes.STREAM, this::stream);
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
        if (!catalog.version().equals(version)) {
            throw new BadRequestResponse("The catalog is version " + catalog.version());
        }
        catalogs.store(version, ctx.body());
        ctx.status(HttpStatus.NO_CONTENT);
    }

    private void open(Context ctx) {
        if (!limiter.tryAcquire(ctx.ip())) {
            throw new TooManyRequestsResponse("Too many sessions opened, try again later");
        }
        CreateSession request = ctx.bodyAsClass(CreateSession.class);
        if (request.payload() == null || !request.payload().isObject()) throw new BadRequestResponse("Missing payload");
        String name = name(request.participant());
        Catalog catalog = catalogs.find(request.catalog())
                .orElseThrow(
                        () -> new ConflictResponse("Unknown catalog %s, upload it first".formatted(request.catalog())));
        String id = Tokens.sessionId();
        String pullToken = Tokens.pullToken();
        sessions.create(
                id,
                Tokens.hash(pullToken),
                request.catalog(),
                MAPPER.writeValueAsString(request.payload()),
                MAPPER.writeValueAsString(new PayloadValidator(catalog).validate(request.payload())));
        String participantToken = Tokens.pullToken();
        participants.add(id, Tokens.hash(participantToken), name);
        StoredSession stored = sessions.find(id).orElseThrow();
        ctx.status(HttpStatus.CREATED)
                .json(new SessionCreated(id, editorUrl(id, participantToken), pullToken, stored.expires()));
    }

    private void addParticipant(Context ctx) {
        StoredSession session = pulling(ctx);
        String name = name(ctx.bodyAsClass(AddParticipant.class).name());
        String token = Tokens.pullToken();
        participants.add(session.id(), Tokens.hash(token), name);
        ctx.status(HttpStatus.CREATED).json(new ParticipantCreated(name, editorUrl(session.id(), token)));
    }

    private void view(Context ctx) {
        StoredSession session = session(ctx);
        if (!isPuller(ctx, session)) editing(ctx, session);
        ctx.json(view(session));
    }

    private void lock(Context ctx) {
        StoredSession session = session(ctx);
        Participant participant = editing(ctx, session);
        String section = ctx.bodyAsClass(LockRequest.class).section();
        if (!rooms.of(session.id()).take(participant, section)) {
            throw locked(rooms.of(session.id()).locks().stream()
                    .filter(lock -> lock.section().equals(section))
                    .toList());
        }
        ctx.status(HttpStatus.NO_CONTENT);
    }

    private void unlock(Context ctx) {
        StoredSession session = session(ctx);
        Participant participant = editing(ctx, session);
        rooms.of(session.id())
                .release(participant, ctx.bodyAsClass(LockRequest.class).section());
        ctx.status(HttpStatus.NO_CONTENT);
    }

    private void save(Context ctx) {
        StoredSession session = session(ctx);
        ctx.json(change(session, editing(ctx, session), replaceAll(MAPPER.readTree(ctx.body()))));
    }

    private void changes(Context ctx) {
        StoredSession session = session(ctx);
        Participant participant = editing(ctx, session);
        ChangeBatch batch = ctx.bodyAsClass(ChangeBatch.class);
        if (batch.operations() == null) throw new BadRequestResponse("Missing operations");
        ctx.json(change(session, participant, batch.operations()));
    }

    private void revisions(Context ctx) {
        StoredSession session = session(ctx);
        editing(ctx, session);
        List<RevisionInfo> revisions = new ArrayList<>();
        revisions.add(new RevisionInfo(0, session.created(), 0, null));
        for (StoredRevision revision : sessions.revisions(session.id(), session.revision())) {
            revisions.add(new RevisionInfo(
                    revision.revision(),
                    revision.created(),
                    MAPPER.readTree(revision.operations()).size(),
                    revision.author()));
        }
        ctx.json(revisions);
    }

    private void revision(Context ctx) {
        StoredSession session = session(ctx);
        editing(ctx, session);
        ctx.json(state(session, revisionNumber(ctx, session)));
    }

    private void restore(Context ctx) {
        StoredSession session = session(ctx);
        Participant participant = editing(ctx, session);
        JsonNode payload = state(session, revisionNumber(ctx, session));
        ctx.json(change(session, participant, replaceAll(payload)));
    }

    private void submit(Context ctx) {
        StoredSession session = session(ctx);
        editing(ctx, session);
        if (!sessions.submit(session.id())) throw new NotFoundResponse("No such session");
        rooms.of(session.id()).broadcast(StreamEvent.of(StreamEventType.SUBMITTED));
        ctx.status(HttpStatus.NO_CONTENT);
    }

    private void result(Context ctx) {
        StoredSession session = pulling(ctx);
        if (session.state() != SessionState.SUBMITTED) throw new ConflictResponse("The session is not submitted yet");
        ctx.json(view(session));
    }

    private void close(Context ctx) {
        StoredSession session = pulling(ctx);
        sessions.delete(session.id());
        rooms.close(session.id());
        ctx.status(HttpStatus.NO_CONTENT);
    }

    private void stream(WsConfig ws) {
        ws.onConnect(ctx -> {
            Optional<Participant> participant = streaming(ctx);
            if (participant.isEmpty()) {
                ctx.closeSession(4401, "Unknown session or token");
                return;
            }
            StoredSession session = sessions.find(ctx.pathParam("id")).orElseThrow();
            rooms.of(session.id()).join(ctx.sessionId(), participant.get(), ctx::send, session.revision());
        });
        ws.onMessage(ctx -> {
            if (!ctx.message().contains("ping")) return;
            streaming(ctx)
                    .ifPresent(participant -> rooms.of(ctx.pathParam("id")).seen(participant));
        });
        ws.onClose(ctx -> rooms.of(ctx.pathParam("id")).leave(ctx.sessionId()));
    }

    private Optional<Participant> streaming(WsContext ctx) {
        String token = ctx.queryParam("token");
        if (token == null) return Optional.empty();
        String id = ctx.pathParam("id");
        if (sessions.find(id).isEmpty()) return Optional.empty();
        return participants.find(id, token);
    }

    private ChangeAccepted change(StoredSession opened, Participant participant, JsonNode operations) {
        Room room = rooms.of(opened.id());
        StoredSession session = opened;
        for (int attempt = 0; attempt < SAVE_ATTEMPTS; attempt++) {
            JsonNode current = MAPPER.readTree(session.payload());
            JsonNode payload;
            try {
                List<LockInfo> blocking = room.claim(participant, Sections.of(current, operations));
                if (!blocking.isEmpty()) throw locked(blocking);
                payload = JsonPatch.apply(current, operations);
            } catch (JsonPatch.PatchException e) {
                throw new BadRequestResponse(e.getMessage());
            }
            if (!payload.isObject()) throw new BadRequestResponse("The payload must stay an object");
            List<PayloadProblem> problems = catalogs.find(session.catalog())
                    .map(catalog -> new PayloadValidator(catalog).validate(payload))
                    .orElse(List.of());
            boolean saved = sessions.saveRevision(
                    session.id(),
                    session.revision(),
                    participant.name(),
                    MAPPER.writeValueAsString(operations),
                    MAPPER.writeValueAsString(payload),
                    MAPPER.writeValueAsString(problems));
            if (saved) {
                int revision = session.revision() + 1;
                room.broadcast(StreamEvent.change(revision, participant.name(), operations));
                return new ChangeAccepted(revision, problems);
            }
            session = sessions.find(opened.id()).orElseThrow(() -> new NotFoundResponse("No such session"));
        }
        throw new ConflictResponse("The session is too busy, try again");
    }

    /**
     * Rebuilds the payload at a revision by replaying the stored operations on the payload the session opened with.
     */
    private JsonNode state(StoredSession session, int revision) {
        JsonNode payload = MAPPER.readTree(
                sessions.initialPayload(session.id()).orElseThrow(() -> new NotFoundResponse("No such session")));
        for (StoredRevision stored : sessions.revisions(session.id(), revision)) {
            payload = JsonPatch.apply(payload, MAPPER.readTree(stored.operations()));
        }
        return payload;
    }

    private String editorUrl(String id, String participantToken) {
        return config.editorUrl().replace("{id}", id).replace("{token}", participantToken);
    }

    private static String name(String name) {
        String stripped = name == null ? "" : name.strip();
        if (stripped.isEmpty() || stripped.length() > MAX_NAME) {
            throw new BadRequestResponse("A participant needs a name of 1 to %d characters".formatted(MAX_NAME));
        }
        return stripped;
    }

    private static JsonNode replaceAll(JsonNode payload) {
        ArrayNode operations = MAPPER.createArrayNode();
        operations.addObject().put("op", "replace").put("path", "").set("value", payload);
        return operations;
    }

    private static int revisionNumber(Context ctx, StoredSession session) {
        int revision = ctx.pathParamAsClass("revision", Integer.class).get();
        if (revision < 0 || revision > session.revision()) throw new NotFoundResponse("No such revision");
        return revision;
    }

    private static HttpResponseException locked(List<LockInfo> blocking) {
        return new HttpResponseException(
                HttpStatus.LOCKED,
                "Held by others: "
                        + blocking.stream()
                                .map(lock -> lock.section() + " (" + lock.holder() + ")")
                                .collect(Collectors.joining(", ")),
                blocking.stream().collect(Collectors.toMap(LockInfo::section, LockInfo::holder, (a, b) -> a)));
    }

    private StoredSession session(Context ctx) {
        return sessions.find(ctx.pathParam("id")).orElseThrow(() -> new NotFoundResponse("No such session"));
    }

    private Participant editing(Context ctx, StoredSession session) {
        Participant participant = participants
                .find(session.id(), bearer(ctx))
                .orElseThrow(() -> new UnauthorizedResponse("Unknown participant token"));
        rooms.of(session.id()).seen(participant);
        return participant;
    }

    private StoredSession pulling(Context ctx) {
        StoredSession session = session(ctx);
        if (!isPuller(ctx, session)) throw new UnauthorizedResponse("Wrong pull token");
        return session;
    }

    private static boolean isPuller(Context ctx, StoredSession session) {
        return Tokens.matches(bearer(ctx), session.pullTokenHash());
    }

    private static String bearer(Context ctx) {
        String header = ctx.header("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.substring(7) : "";
    }

    private SessionView view(StoredSession session) {
        Room room = rooms.of(session.id());
        return new SessionView(
                session.id(),
                session.state(),
                session.revision(),
                session.catalog(),
                MAPPER.readTree(session.payload()),
                MAPPER.readValue(session.problems(), PROBLEMS),
                room.locks(),
                room.online(),
                session.expires());
    }
}
