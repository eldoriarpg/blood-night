/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor.stream;

/**
 * What a {@link StreamEvent} tells.
 */
public enum StreamEventType {
    /**
     * Sent once on connect: revision, locks and who is online.
     */
    HELLO,
    /**
     * A revision was saved: revision, author and operations.
     */
    CHANGE,
    /**
     * A section was taken: section and holder.
     */
    LOCK,
    /**
     * A section was released: section.
     */
    UNLOCK,
    /**
     * Who is online changed: participants.
     */
    PRESENCE,
    /**
     * The session was marked as done.
     */
    SUBMITTED,
    /**
     * The server applied the session and keeps it open: the applied revision.
     */
    APPLIED,
    /**
     * The server cancelled the session, or replaced it with a new one; it is gone.
     */
    CLOSED
}
