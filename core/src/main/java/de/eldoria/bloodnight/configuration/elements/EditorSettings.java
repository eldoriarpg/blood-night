/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements;

/**
 * Where the web editor's service runs.
 */
public class EditorSettings {
    // TODO: default to the hosted editor service once it runs
    private String url = "http://localhost:8080";
    private int timeoutSeconds = 30;

    /**
     * @return the service's base URL, without a trailing slash
     */
    public String url() {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * @return how long a request may take, at least one second
     */
    public int timeoutSeconds() {
        return Math.max(timeoutSeconds, 1);
    }
}
