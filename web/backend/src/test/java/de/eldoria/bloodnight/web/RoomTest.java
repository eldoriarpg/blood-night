/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.web.api.editor.EditorArea;
import de.eldoria.bloodnight.web.api.editor.LockInfo;
import de.eldoria.bloodnight.web.api.editor.StreamEvent;
import de.eldoria.bloodnight.web.api.editor.StreamEventType;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomTest {
    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-10-11T12:00:00Z"));
    private final Room room = new Room(
            new Clock() {
                @Override
                public ZoneOffset getZone() {
                    return ZoneOffset.UTC;
                }

                @Override
                public Clock withZone(ZoneId zone) {
                    return this;
                }

                @Override
                public Instant instant() {
                    return now.get();
                }
            },
            Duration.ofMinutes(2));
    private final Participant steve = new Participant("s", "Steve", EnumSet.allOf(EditorArea.class));
    private final Participant alex = new Participant("a", "Alex", EnumSet.allOf(EditorArea.class));

    private static Sections.Touched touching(String... sections) {
        return new Sections.Touched(Set.of(sections), false);
    }

    @Test
    void sectionsHeldByOthersBlock() {
        assertEquals(List.of(), room.claim(steve, touching("mobs/a")));
        assertEquals(List.of(), room.claim(steve, touching("mobs/a", "mobs/b")));
        assertEquals(List.of(new LockInfo("mobs/a", "Steve")), room.claim(alex, touching("mobs/a", "mobs/c")));
        assertEquals(List.of(), room.claim(alex, touching("mobs/c")));
        assertEquals(2, room.claim(alex, new Sections.Touched(Set.of(), true)).size());
        assertFalse(room.take(alex, "mobs/b"));
        room.release(steve, "mobs/b");
        assertTrue(room.take(alex, "mobs/b"));
    }

    @Test
    void streamsHearLocksAndPresence() {
        List<StreamEvent> heard = new ArrayList<>();
        room.join("c1", alex, heard::add, 4);
        assertEquals(StreamEventType.HELLO, heard.getFirst().type());
        assertEquals(4, heard.getFirst().revision());
        room.join("c2", steve, event -> {}, 4);
        room.take(steve, "settings/main");
        room.leave("c2");
        assertEquals(
                List.of(
                        StreamEventType.HELLO,
                        StreamEventType.PRESENCE,
                        StreamEventType.PRESENCE,
                        StreamEventType.LOCK,
                        StreamEventType.UNLOCK,
                        StreamEventType.PRESENCE),
                heard.stream().map(StreamEvent::type).toList());
        assertEquals(List.of("Alex"), room.online());
    }

    @Test
    void absentParticipantsLoseTheirSections() {
        room.take(steve, "mobs/a");
        room.join("c1", alex, event -> {}, 0);
        room.take(alex, "mobs/b");
        now.set(now.get().plus(Duration.ofMinutes(3)));
        room.expire();
        assertEquals(List.of(new LockInfo("mobs/b", "Alex")), room.locks());
    }
}
