/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.storage;

import de.eldoria.bloodnight.web.api.editor.LastApplied;
import de.eldoria.bloodnight.web.api.editor.SessionState;
import org.jspecify.annotations.Nullable;

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
 * @param created         when the server opened it
 * @param expires         when it is deleted unless used again
 * @param appliedRevision the revision the server last applied; none before the first apply
 * @param appliedAt       when the server last applied it; none before the first apply
 */
public record StoredSession(
        String id,
        String pullTokenHash,
        SessionState state,
        int revision,
        String catalog,
        String payload,
        String problems,
        Instant created,
        Instant expires,
        @Nullable Integer appliedRevision,
        @Nullable Instant appliedAt) {
    /**
     * @return when the server last applied the session; null before the first apply
     */
    @Nullable
    public LastApplied lastApplied() {
        Integer revision = appliedRevision;
        Instant at = appliedAt;
        return revision == null || at == null ? null : new LastApplied(revision, at);
    }
}
