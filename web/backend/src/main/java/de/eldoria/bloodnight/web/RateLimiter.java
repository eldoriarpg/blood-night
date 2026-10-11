/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows each address a number of actions per sliding window.
 */
final class RateLimiter {
    private final int limit;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> actions = new HashMap<>();

    /**
     * @param limit  actions per window
     * @param window the window
     * @param clock  the time
     */
    RateLimiter(int limit, Duration window, Clock clock) {
        this.limit = limit;
        this.window = window;
        this.clock = clock;
    }

    /**
     * Counts an action of an address if it is still allowed.
     *
     * @param address the address
     * @return whether the action is allowed
     */
    synchronized boolean tryAcquire(String address) {
        Instant now = clock.instant();
        Instant start = now.minus(window);
        Deque<Instant> recent = actions.computeIfAbsent(address, ignored -> new ArrayDeque<>());
        while (!recent.isEmpty() && !recent.peekFirst().isAfter(start)) recent.pollFirst();
        actions.values().removeIf(Deque::isEmpty);
        if (recent.size() >= limit) return false;
        recent.addLast(now);
        actions.put(address, recent);
        return true;
    }
}
