/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements;

/**
 * Where the web editor's service runs: the hosted one by default, or a server's own.
 */
public class EditorSettings {
    private String url = "https://bloodnight.eldoria.de";
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
