/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.chojo.sadu.mapper.wrapper.Row;
import de.eldoria.bloodnight.web.api.editor.SessionState;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Stores editor sessions and their revisions. Every use moves a session's expiry to the lifetime from now; expired
 * sessions are not found and deleted by {@link #deleteExpired()}.
 * <p>
 * Revision 0 is the payload the session was opened with; each later revision stores the operations that led to it,
 * so any revision can be rebuilt by replaying them. A revision is only saved on top of the revision its changes were
 * applied to, checked and written in one statement, so concurrent saves never lose each other's changes.
 */
final class SessionRepository {
    private static final String COLUMNS =
            "id, pull_token_hash, state, revision, catalog, payload::TEXT AS payload, problems::TEXT AS problems,"
                    + " created, expires, applied_revision, applied_at";
    private final Duration lifetime;

    /**
     * @param lifetime how long a session lives without being used
     */
    SessionRepository(Duration lifetime) {
        this.lifetime = lifetime;
    }

    void create(String id, String pullTokenHash, String catalog, String payload, String problems) {
        query("""
                INSERT INTO editor_session(id, pull_token_hash, catalog, payload, initial_payload, problems, state, expires)
                VALUES (:id, :hash, :catalog, CAST(:payload AS JSONB), CAST(:payload AS JSONB), CAST(:problems AS JSONB),
                        :state, now() + make_interval(secs => :lifetime));""")
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
        return query("SELECT %s FROM editor_session WHERE id = :id AND expires > now();", COLUMNS)
                .single(call().bind("id", id))
                .map(SessionRepository::map)
                .first();
    }

    /**
     * Saves a new revision unless another save came first since {@code base} was read.
     *
     * @param id         the session
     * @param base       the revision the change was applied to
     * @param author     who made the change
     * @param operations the operations of the change, JSON
     * @param payload    the payload after the change, JSON
     * @param problems   the problems found in it, JSON
     * @return whether the revision was saved
     */
    boolean saveRevision(String id, int base, String author, String operations, String payload, String problems) {
        return query("""
                WITH updated AS (
                    UPDATE editor_session
                    SET payload = CAST(:payload AS JSONB), problems = CAST(:problems AS JSONB), state = :state,
                        revision = revision + 1, expires = now() + make_interval(secs => :lifetime)
                    WHERE id = :id AND revision = :base AND expires > now()
                    RETURNING id, revision)
                INSERT INTO editor_revision(session_id, revision, operations, author)
                SELECT id, revision, CAST(:operations AS JSONB), :author FROM updated;""")
                .single(call().bind("id", id)
                        .bind("base", base)
                        .bind("author", author)
                        .bind("operations", operations)
                        .bind("payload", payload)
                        .bind("problems", problems)
                        .bind("state", SessionState.EDITING.name())
                        .bind("lifetime", lifetime.toSeconds()))
                .insert()
                .changed();
    }

    /**
     * @param id the session
     * @return the payload the session was opened with, JSON
     */
    Optional<String> initialPayload(String id) {
        return query("SELECT initial_payload::TEXT AS payload FROM editor_session WHERE id = :id AND expires > now();")
                .single(call().bind("id", id))
                .map(row -> row.getString("payload"))
                .first();
    }

    /**
     * @param id the session
     * @param to the last revision to return
     * @return the revisions 1 to {@code to}, in order
     */
    List<StoredRevision> revisions(String id, int to) {
        return query("""
                SELECT revision, operations::TEXT AS operations, author, created FROM editor_revision
                WHERE session_id = :id AND revision <= :to ORDER BY revision;""")
                .single(call().bind("id", id).bind("to", to))
                .map(row -> new StoredRevision(
                        row.getInt("revision"),
                        row.getString("operations"),
                        row.getString("author"),
                        row.getTimestamp("created").toInstant()))
                .all();
    }

    boolean submit(String id) {
        return query("""
                UPDATE editor_session SET state = :state, expires = now() + make_interval(secs => :lifetime)
                WHERE id = :id AND expires > now();""")
                .single(call().bind("id", id)
                        .bind("state", SessionState.SUBMITTED.name())
                        .bind("lifetime", lifetime.toSeconds()))
                .update()
                .changed();
    }

    /**
     * Records that the server applied a revision and keeps the session. The session counts as applied only when
     * nobody changed it since that revision; the files' hashes after the apply replace the payload's, in the current
     * and the initial payload, so every revision checks the next apply against what this one wrote.
     *
     * @param id       the session
     * @param revision the revision the server applied
     * @param hashes   the server's file hashes after the apply, JSON
     * @return whether the session exists
     */
    boolean applied(String id, int revision, String hashes) {
        return query("""
                UPDATE editor_session
                SET state = CASE WHEN revision = :revision THEN :state ELSE state END,
                    applied_revision = :revision, applied_at = now(),
                    payload = jsonb_set(payload, '{hashes}', CAST(:hashes AS JSONB)),
                    initial_payload = jsonb_set(initial_payload, '{hashes}', CAST(:hashes AS JSONB)),
                    expires = now() + make_interval(secs => :lifetime)
                WHERE id = :id AND expires > now();""")
                .single(call().bind("id", id)
                        .bind("revision", revision)
                        .bind("hashes", hashes)
                        .bind("state", SessionState.APPLIED.name())
                        .bind("lifetime", lifetime.toSeconds()))
                .update()
                .changed();
    }

    boolean delete(String id) {
        return query("DELETE FROM editor_session WHERE id = :id;")
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
        int sessions = query("DELETE FROM editor_session WHERE expires <= now();")
                .single()
                .delete()
                .rows();
        query("""
                DELETE FROM editor_catalog c
                WHERE created < now() - INTERVAL '1 day'
                  AND NOT EXISTS (SELECT 1 FROM editor_session s WHERE s.catalog = c.version);""").single().delete();
        return sessions;
    }

    private static StoredSession map(Row row) throws SQLException {
        return new StoredSession(
                row.getString("id"),
                row.getString("pull_token_hash"),
                SessionState.valueOf(row.getString("state")),
                row.getInt("revision"),
                row.getString("catalog"),
                row.getString("payload"),
                row.getString("problems"),
                row.getTimestamp("created").toInstant(),
                row.getTimestamp("expires").toInstant(),
                (Integer) row.getObject("applied_revision"),
                row.getTimestamp("applied_at") == null
                        ? null
                        : row.getTimestamp("applied_at").toInstant());
    }
}
