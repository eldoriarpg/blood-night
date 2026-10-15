/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.List;

/**
 * A message the service streams to everyone editing a session, see {@link EditorRoutes#STREAM}. Fields not used by
 * an event's type are left out.
 *
 * @param type         what happened
 * @param revision     the session's revision ({@code HELLO}, {@code CHANGE}), the applied one ({@code APPLIED})
 * @param author       who saved the change ({@code CHANGE})
 * @param operations   the change's JSON Patch operations ({@code CHANGE})
 * @param section      the section ({@code LOCK}, {@code UNLOCK})
 * @param holder       who holds it ({@code LOCK})
 * @param participants who is online ({@code HELLO}, {@code PRESENCE})
 * @param locks        all held sections ({@code HELLO})
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record StreamEvent(
        StreamEventType type,
        @Nullable Integer revision,
        @Nullable String author,
        @Nullable JsonNode operations,
        @Nullable String section,
        @Nullable String holder,
        @Nullable List<String> participants,
        @Nullable List<LockInfo> locks) {

    public static StreamEvent hello(int revision, List<String> participants, List<LockInfo> locks) {
        return new StreamEvent(StreamEventType.HELLO, revision, null, null, null, null, participants, locks);
    }

    public static StreamEvent change(int revision, String author, JsonNode operations) {
        return new StreamEvent(StreamEventType.CHANGE, revision, author, operations, null, null, null, null);
    }

    public static StreamEvent lock(String section, String holder) {
        return new StreamEvent(StreamEventType.LOCK, null, null, null, section, holder, null, null);
    }

    public static StreamEvent unlock(String section) {
        return new StreamEvent(StreamEventType.UNLOCK, null, null, null, section, null, null, null);
    }

    public static StreamEvent presence(List<String> participants) {
        return new StreamEvent(StreamEventType.PRESENCE, null, null, null, null, null, participants, null);
    }

    public static StreamEvent applied(int revision) {
        return new StreamEvent(StreamEventType.APPLIED, revision, null, null, null, null, null, null);
    }

    public static StreamEvent of(StreamEventType type) {
        return new StreamEvent(type, null, null, null, null, null, null, null);
    }
}
