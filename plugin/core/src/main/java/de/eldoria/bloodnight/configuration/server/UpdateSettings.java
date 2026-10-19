/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.server;

/**
 * Whether the server is told about new BloodNight versions.
 */
public class UpdateSettings {
    private boolean notice = true;

    /**
     * @return whether the console and admins hear about a new version
     */
    public boolean notice() {
        return notice;
    }
}
