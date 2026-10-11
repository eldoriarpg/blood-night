/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.service;

import de.chojo.sadu.mapper.wrapper.Row;
import de.eldoria.bloodnight.editor.api.SessionState;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Stores editor sessions. Every use moves a session's expiry to the lifetime from now; expired sessions are not found
 * and deleted by {@link #deleteExpired()}.
 */
final class SessionRepository {
    private static final String COLUMNS =
            "id, pull_token_hash, state, catalog, payload::TEXT AS payload," + " problems::TEXT AS problems, expires";
    private final Duration lifetime;

    /**
     * @param lifetime how long a session lives without being used
     */
    SessionRepository(Duration lifetime) {
        this.lifetime = lifetime;
    }

    void create(String id, String pullTokenHash, String catalog, String payload, String problems) {
        query("""
                INSERT INTO session(id, pull_token_hash, catalog, payload, problems, state, expires)
                VALUES (:id, :hash, :catalog, CAST(:payload AS JSONB), CAST(:problems AS JSONB), :state,
                        now() + make_interval(secs => :lifetime));""")
                .single(call().bind("id", id)
                        .bind("hash", pullTokenHash)
                        .bind("catalog", catalog)
                        .bind("payload", payload)
                        .bind("problems", problems)
                        .bind("state", SessionState.EDITING.name())
                        .bind("lifetime", lifetime.toSeconds()))
                .insert();
    }

    Optional<StoredSession> find(String id) {
        return query("SELECT %s FROM session WHERE id = :id AND expires > now();", COLUMNS)
                .single(call().bind("id", id))
                .map(SessionRepository::map)
                .first();
    }

    boolean save(String id, String payload, String problems) {
        return query("""
                UPDATE session
                SET payload = CAST(:payload AS JSONB), problems = CAST(:problems AS JSONB), state = :state,
                    expires = now() + make_interval(secs => :lifetime)
                WHERE id = :id AND expires > now();""")
                .single(call().bind("id", id)
                        .bind("payload", payload)
                        .bind("problems", problems)
                        .bind("state", SessionState.EDITING.name())
                        .bind("lifetime", lifetime.toSeconds()))
                .update()
                .changed();
    }

    boolean submit(String id) {
        return query("""
                UPDATE session SET state = :state, expires = now() + make_interval(secs => :lifetime)
                WHERE id = :id AND expires > now();""")
                .single(call().bind("id", id)
                        .bind("state", SessionState.SUBMITTED.name())
                        .bind("lifetime", lifetime.toSeconds()))
                .update()
                .changed();
    }

    boolean delete(String id) {
        return query("DELETE FROM session WHERE id = :id;")
                .single(call().bind("id", id))
                .delete()
                .changed();
    }

    /**
     * Deletes expired sessions and catalogs no session uses any more.
     *
     * @return how many sessions were deleted
     */
    int deleteExpired() {
        int sessions = query("DELETE FROM session WHERE expires <= now();")
                .single()
                .delete()
                .rows();
        query("""
                DELETE FROM catalog c
                WHERE created < now() - INTERVAL '1 day'
                  AND NOT EXISTS (SELECT 1 FROM session s WHERE s.catalog = c.version);""").single().delete();
        return sessions;
    }

    private static StoredSession map(Row row) throws SQLException {
        return new StoredSession(
                row.getString("id"),
                row.getString("pull_token_hash"),
                SessionState.valueOf(row.getString("state")),
                row.getString("catalog"),
                row.getString("payload"),
                row.getString("problems"),
                row.getTimestamp("expires").toInstant());
    }
}
