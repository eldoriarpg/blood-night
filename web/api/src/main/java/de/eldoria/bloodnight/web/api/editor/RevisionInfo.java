/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * One revision of a session; revision 0 is the payload the server opened the session with.
 *
 * @param revision   its number
 * @param created    when it was saved
 * @param operations how many operations it applied; 0 for revision 0
 * @param author     who saved it; none for revision 0
 */
public record RevisionInfo(
        int revision,
        Instant created,
        int operations,
        @Nullable String author) {}
