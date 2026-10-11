/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.web.api.editor.LockInfo;
import de.eldoria.bloodnight.web.api.editor.StreamEvent;
import de.eldoria.bloodnight.web.api.editor.StreamEventType;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Who works on a session right now: the sections they hold and their open streams.
 * <p>
 * A participant is present while one of their streams is open or for {@link #timeout} after they were last heard of.
 * Their sections are released when their last stream closes, or by {@link #expire()} once they are no longer present.
 * Every change of locks and presence is streamed to everyone connected.
 */
final class Room {
    private final Clock clock;
    private final Duration timeout;
    private final Map<String, Participant> locks = new LinkedHashMap<>();
    private final Map<String, Connection> connections = new LinkedHashMap<>();
    private final Map<String, Instant> lastSeen = new HashMap<>();

    private record Connection(Participant participant, Consumer<StreamEvent> send) {}

    Room(Clock clock, Duration timeout) {
        this.clock = clock;
        this.timeout = timeout;
    }

    synchronized void seen(Participant participant) {
        lastSeen.put(participant.tokenHash(), clock.instant());
    }

    /**
     * Opens a stream: the participant gets a hello, everyone the new presence.
     */
    synchronized void join(String connection, Participant participant, Consumer<StreamEvent> send, int revision) {
        seen(participant);
        connections.put(connection, new Connection(participant, send));
        send.accept(StreamEvent.hello(revision, online(), locks()));
        broadcast(StreamEvent.presence(online()));
    }

    /**
     * Closes a stream; the participant's sections are released when it was their last one.
     */
    synchronized void leave(String connection) {
        Connection left = connections.remove(connection);
        if (left == null) return;
        seen(left.participant());
        if (!isConnected(left.participant())) releaseAll(left.participant());
        broadcast(StreamEvent.presence(online()));
    }

    /**
     * Takes every section a change touches for the participant unless another participant holds one of them.
     *
     * @return the sections others hold that block the change; empty when the participant now holds all of them
     */
    synchronized List<LockInfo> claim(Participant participant, Sections.Touched touched) {
        seen(participant);
        expire();
        List<LockInfo> blocking = new ArrayList<>();
        locks.forEach((section, holder) -> {
            boolean other = !holder.tokenHash().equals(participant.tokenHash());
            if (other && (touched.everything() || touched.sections().contains(section))) {
                blocking.add(new LockInfo(section, holder.name()));
            }
        });
        if (blocking.isEmpty()) touched.sections().forEach(section -> take(participant, section));
        return blocking;
    }

    /**
     * @return whether the participant holds the section now
     */
    synchronized boolean take(Participant participant, String section) {
        seen(participant);
        expire();
        Participant holder = locks.get(section);
        if (holder != null) return holder.tokenHash().equals(participant.tokenHash());
        locks.put(section, participant);
        broadcast(StreamEvent.lock(section, participant.name()));
        return true;
    }

    synchronized void release(Participant participant, String section) {
        seen(participant);
        Participant holder = locks.get(section);
        if (holder == null || !holder.tokenHash().equals(participant.tokenHash())) return;
        locks.remove(section);
        broadcast(StreamEvent.unlock(section));
    }

    synchronized List<LockInfo> locks() {
        List<LockInfo> held = new ArrayList<>();
        locks.forEach((section, holder) -> held.add(new LockInfo(section, holder.name())));
        return held;
    }

    synchronized List<String> online() {
        return connections.values().stream()
                .map(connection -> connection.participant().name())
                .distinct()
                .sorted()
                .toList();
    }

    synchronized void broadcast(StreamEvent event) {
        connections.values().forEach(connection -> connection.send().accept(event));
    }

    /**
     * Releases the sections of participants that are no longer present.
     */
    synchronized void expire() {
        Instant limit = clock.instant().minus(timeout);
        List<Participant> absent = locks.values().stream()
                .distinct()
                .filter(holder -> !isConnected(holder))
                .filter(holder -> Objects.requireNonNullElse(lastSeen.get(holder.tokenHash()), Instant.MIN)
                        .isBefore(limit))
                .toList();
        absent.forEach(this::releaseAll);
    }

    /**
     * Tells everyone the session is gone.
     */
    synchronized void close() {
        broadcast(StreamEvent.of(StreamEventType.CLOSED));
        connections.clear();
        locks.clear();
    }

    private boolean isConnected(Participant participant) {
        return connections.values().stream()
                .anyMatch(connection -> connection.participant().tokenHash().equals(participant.tokenHash()));
    }

    private void releaseAll(Participant participant) {
        List<String> held = locks.entrySet().stream()
                .filter(entry -> entry.getValue().tokenHash().equals(participant.tokenHash()))
                .map(Map.Entry::getKey)
                .toList();
        held.forEach(section -> {
            locks.remove(section);
            broadcast(StreamEvent.unlock(section));
        });
    }
}
