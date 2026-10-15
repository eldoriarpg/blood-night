/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor;

/**
 * The editor's routes in BloodNight's web backend, version 1.
 * <ul>
 *   <li>{@code HEAD} / {@code GET} / {@code PUT} {@value #CATALOG}: whether the service has a catalog, read it,
 *   upload it</li>
 *   <li>{@code POST} {@value #SESSIONS}: open a session ({@link CreateSession} to {@link SessionCreated})</li>
 *   <li>{@code GET} {@value #SESSION}: the session ({@link SessionView})</li>
 *   <li>{@code POST} {@value #PARTICIPANTS}: let someone else edit ({@link AddParticipant} to
 *   {@link ParticipantCreated}), with the pull token</li>
 *   <li>{@code POST} / {@code DELETE} {@value #LOCKS}: take or release a section ({@link LockRequest})</li>
 *   <li>{@code WS} {@value #STREAM}{@code ?token=<participant token>}: {@link StreamEvent}s; clients send
 *   {@code ping} to stay present</li>
 *   <li>{@code PUT} {@value #PAYLOAD}: replace the whole payload as a new revision ({@link ChangeAccepted})</li>
 *   <li>{@code POST} {@value #CHANGES}: apply a {@link ChangeBatch} as a new revision ({@link ChangeAccepted})</li>
 *   <li>{@code GET} {@value #REVISIONS}: the session's revisions ({@link RevisionInfo})</li>
 *   <li>{@code GET} {@value #REVISION}: the payload as it was at a revision</li>
 *   <li>{@code POST} {@value #RESTORE}: make a revision's payload the newest revision ({@link ChangeAccepted})</li>
 *   <li>{@code POST} {@value #SUBMIT}: mark the session as done</li>
 *   <li>{@code GET} {@value #RESULT}: the submitted session, with the pull token as bearer</li>
 *   <li>{@code POST} {@value #APPLIED}: the server applied the session and keeps it open ({@link AppliedReport}), with
 *   the pull token as bearer</li>
 *   <li>{@code DELETE} {@value #SESSION}: close the session, with the pull token as bearer</li>
 * </ul>
 * Editing calls carry a participant token as bearer, the server's calls ({@code result}, {@code applied},
 * {@code participants}, closing) its pull token; reading the session accepts either. Changes apply on the latest revision and may only
 * touch sections that are free or held by the sender, otherwise they are refused with {@code 423 Locked}; whole saves
 * and restores need every section free of others.
 */
public final class EditorRoutes {
    public static final String CATALOG = "/api/v1/editor/catalogs/{version}";
    public static final String SESSIONS = "/api/v1/editor/sessions";
    public static final String SESSION = "/api/v1/editor/sessions/{id}";
    public static final String PAYLOAD = "/api/v1/editor/sessions/{id}/payload";
    public static final String CHANGES = "/api/v1/editor/sessions/{id}/changes";
    public static final String REVISIONS = "/api/v1/editor/sessions/{id}/revisions";
    public static final String REVISION = "/api/v1/editor/sessions/{id}/revisions/{revision}";
    public static final String RESTORE = "/api/v1/editor/sessions/{id}/revisions/{revision}/restore";
    public static final String PARTICIPANTS = "/api/v1/editor/sessions/{id}/participants";
    public static final String LOCKS = "/api/v1/editor/sessions/{id}/locks";
    public static final String STREAM = "/api/v1/editor/sessions/{id}/stream";
    public static final String SUBMIT = "/api/v1/editor/sessions/{id}/submit";
    public static final String RESULT = "/api/v1/editor/sessions/{id}/result";
    public static final String APPLIED = "/api/v1/editor/sessions/{id}/applied";

    private EditorRoutes() {}

    /**
     * @param route  a route with placeholders
     * @param values the values for the route's placeholders, in order
     * @return the path
     */
    public static String path(String route, Object... values) {
        String path = route;
        for (Object value : values) path = path.replaceFirst("\\{[a-z]+}", String.valueOf(value));
        return path;
    }
}
