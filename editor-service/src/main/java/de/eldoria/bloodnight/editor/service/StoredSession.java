/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.service;

import de.eldoria.bloodnight.editor.api.SessionState;

import java.time.Instant;

/**
 * A session as stored.
 *
 * @param id            its id
 * @param pullTokenHash hash of its pull token
 * @param state         where it stands
 * @param catalog       version of the server's catalog
 * @param payload       the payload, JSON
 * @param problems      the problems last found, JSON
 * @param expires       when it is deleted unless used again
 */
record StoredSession(
        String id,
        String pullTokenHash,
        SessionState state,
        String catalog,
        String payload,
        String problems,
        Instant expires) {}
