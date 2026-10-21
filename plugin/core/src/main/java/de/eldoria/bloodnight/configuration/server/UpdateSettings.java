/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.server;

/**
 * Whether the server is told about new BloodNight versions and whether they are downloaded.
 */
public class UpdateSettings {
    private boolean notice = true;
    private boolean autoUpdate = false;
    private boolean askForAutoUpdate = true;

    /**
     * @return whether the console and admins hear about a new version
     */
    public boolean notice() {
        return notice;
    }

    /**
     * @return whether a new stable release is downloaded, to take effect on the next start
     */
    public boolean autoUpdate() {
        return autoUpdate;
    }

    /**
     * @return whether admins are asked in chat to switch auto update on
     */
    public boolean askForAutoUpdate() {
        return askForAutoUpdate;
    }
}
