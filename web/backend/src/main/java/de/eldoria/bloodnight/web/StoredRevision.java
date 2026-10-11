/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import org.jspecify.annotations.Nullable;

import java.time.Instant;

/**
 * A stored revision.
 *
 * @param revision   its number, from 1
 * @param operations the operations that led to it, JSON
 * @param author     who saved it
 * @param created    when it was saved
 */
record StoredRevision(
        int revision, String operations, @Nullable String author, Instant created) {}
