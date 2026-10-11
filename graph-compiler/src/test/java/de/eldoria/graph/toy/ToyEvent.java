/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.toy;

/**
 * Context object of an entry point in the test consumer, shaped like a game event.
 */
public class ToyEvent {
    private final ToyPlayer player;
    private int points;
    private boolean cancelled;

    public ToyEvent(ToyPlayer player, int points) {
        this.player = player;
        this.points = points;
    }

    public ToyPlayer getPlayer() {
        return player;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getHandlers() {
        return "plumbing";
    }
}
