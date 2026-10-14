/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.updates;

import java.util.Optional;

/**
 * Where the newest BloodNight version is looked up.
 */
public interface ReleaseSource {
    /**
     * Asks for the newest version. Blocks, so it is called off the server's threads.
     *
     * @param current the running version
     * @return the newest version when it is newer than the running one; empty when it is not or the source cannot be
     *     reached
     */
    Optional<String> newer(String current);
}
