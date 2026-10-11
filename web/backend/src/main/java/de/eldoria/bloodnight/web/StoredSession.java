/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.web.api.editor.SessionState;

import java.time.Instant;

/**
 * A session as stored.
 *
 * @param id            its id
 * @param pullTokenHash hash of its pull token
 * @param state         where it stands
 * @param revision      its current revision
 * @param catalog       version of the server's catalog
 * @param payload       the payload at the current revision, JSON
 * @param problems      the problems last found, JSON
 * @param created       when the server opened it
 * @param expires       when it is deleted unless used again
 */
record StoredSession(
        String id,
        String pullTokenHash,
        SessionState state,
        int revision,
        String catalog,
        String payload,
        String problems,
        Instant created,
        Instant expires) {}
