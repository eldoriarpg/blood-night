/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import java.util.Optional;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Stores who may edit a session, by the hash of their participant token.
 */
final class ParticipantRepository {
    void add(String session, String tokenHash, String name) {
        query("INSERT INTO editor_participant(session_id, token_hash, name) VALUES (:session, :hash, :name);")
                .single(call().bind("session", session).bind("hash", tokenHash).bind("name", name))
                .insert();
    }

    /**
     * @param session the session
     * @param token   a participant token as sent
     * @return the participant the token belongs to
     */
    Optional<Participant> find(String session, String token) {
        return query(
                        "SELECT token_hash, name FROM editor_participant WHERE session_id = :session AND token_hash = :hash;")
                .single(call().bind("session", session).bind("hash", Tokens.hash(token)))
                .map(row -> new Participant(row.getString("token_hash"), row.getString("name")))
                .first();
    }
}
