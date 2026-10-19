/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.storage;

import de.eldoria.bloodnight.web.api.editor.EditorArea;
import de.eldoria.bloodnight.web.session.Participant;
import de.eldoria.bloodnight.web.session.Tokens;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.chojo.sadu.queries.api.call.Call.call;
import static de.chojo.sadu.queries.api.query.Query.query;

/**
 * Stores who may edit a session, by the hash of their participant token.
 */
public final class ParticipantRepository {
    public void add(String session, String tokenHash, String name, Set<EditorArea> areas) {
        query(
                        "INSERT INTO editor_participant(session_id, token_hash, name, areas) VALUES (:session, :hash, :name, :areas);")
                .single(call().bind("session", session)
                        .bind("hash", tokenHash)
                        .bind("name", name)
                        .bind("areas", areas.stream().map(Enum::name).sorted().collect(Collectors.joining(","))))
                .insert();
    }

    /**
     * @param session the session
     * @param token   a participant token as sent
     * @return the participant the token belongs to
     */
    public Optional<Participant> find(String session, String token) {
        return query(
                        "SELECT token_hash, name, areas FROM editor_participant WHERE session_id = :session AND token_hash = :hash;")
                .single(call().bind("session", session).bind("hash", Tokens.hash(token)))
                .map(row -> new Participant(
                        row.getString("token_hash"), row.getString("name"), areas(row.getString("areas"))))
                .first();
    }

    private static Set<EditorArea> areas(String stored) {
        Set<EditorArea> areas = EnumSet.noneOf(EditorArea.class);
        for (String area : stored.split(",")) {
            if (!area.isBlank()) areas.add(EditorArea.valueOf(area));
        }
        return areas;
    }
}
