/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.session;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The rooms of the sessions being worked on, in memory; the service runs as one instance.
 */
public final class Rooms {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Clock clock;
    private final Duration timeout;

    /**
     * @param clock   the time
     * @param timeout how long a participant without an open stream stays present
     */
    public Rooms(Clock clock, Duration timeout) {
        this.clock = clock;
        this.timeout = timeout;
    }

    public Room of(String session) {
        return rooms.computeIfAbsent(session, ignored -> new Room(clock, timeout));
    }

    /**
     * Tells everyone in a session's room it is gone, and forgets the room.
     */
    public void close(String session) {
        Room room = rooms.remove(session);
        if (room != null) room.close();
    }

    public void expireAll() {
        rooms.values().forEach(Room::expire);
    }
}
