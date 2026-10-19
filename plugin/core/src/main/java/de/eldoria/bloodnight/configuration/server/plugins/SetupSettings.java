/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.server.plugins;

/**
 * Whether the first-run setup was finished. The editor's setup wizard sets {@code done} when it ends; until then admins
 * are reminded to run {@code /bloodnight setup}.
 */
public class SetupSettings {
    private boolean done = false;

    /**
     * @return whether the setup was finished
     */
    public boolean done() {
        return done;
    }
}
