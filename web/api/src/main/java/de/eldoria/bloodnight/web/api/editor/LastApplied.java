/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor;

import java.time.Instant;

/**
 * When the server last applied a session.
 *
 * @param revision the revision it applied
 * @param at       when it applied it
 */
public record LastApplied(int revision, Instant at) {}
