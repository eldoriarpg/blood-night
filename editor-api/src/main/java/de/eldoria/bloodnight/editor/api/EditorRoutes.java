/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.api;

/**
 * The editor service's routes, version 1.
 * <ul>
 *   <li>{@code HEAD} / {@code PUT} {@value #CATALOG}: whether the service has a catalog, upload it</li>
 *   <li>{@code POST} {@value #SESSIONS}: open a session ({@link CreateSession} to {@link SessionCreated})</li>
 *   <li>{@code GET} {@value #SESSION}: the session ({@link SessionView})</li>
 *   <li>{@code PUT} {@value #PAYLOAD}: save the edited payload, answered with the problems found</li>
 *   <li>{@code POST} {@value #SUBMIT}: mark the session as done</li>
 *   <li>{@code GET} {@value #RESULT}: the submitted session, with the pull token as bearer</li>
 *   <li>{@code DELETE} {@value #SESSION}: close the session, with the pull token as bearer</li>
 * </ul>
 */
public final class EditorRoutes {
    public static final String CATALOG = "/api/v1/catalogs/{version}";
    public static final String SESSIONS = "/api/v1/sessions";
    public static final String SESSION = "/api/v1/sessions/{id}";
    public static final String PAYLOAD = "/api/v1/sessions/{id}/payload";
    public static final String SUBMIT = "/api/v1/sessions/{id}/submit";
    public static final String RESULT = "/api/v1/sessions/{id}/result";

    private EditorRoutes() {}

    /**
     * @param route a route with placeholders
     * @param value the value for the route's one placeholder
     * @return the path
     */
    public static String path(String route, String value) {
        return route.replaceFirst("\\{[a-z]+}", value);
    }
}
