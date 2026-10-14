/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements;

import dev.chojo.ocular.override.Env;
import dev.chojo.ocular.override.Overwrite;
import dev.chojo.ocular.override.Prop;

/**
 * Where the web editor's service runs: the hosted one by default, or a server's own. The system property
 * {@code bloodnight.editor.url} or the environment variable {@code BLOODNIGHT_EDITOR_URL} replace the configured URL,
 * e.g. for a local test server.
 */
public class EditorSettings {
    @Overwrite(
            prop = @Prop("bloodnight.editor.url"),
            env = @Env("BLOODNIGHT_EDITOR_URL"),
            description = "Where the web editor's service runs")
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
