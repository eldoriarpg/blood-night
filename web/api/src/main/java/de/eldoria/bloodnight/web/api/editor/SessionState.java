/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor;

/**
 * Where an editor session stands.
 */
public enum SessionState {
    /**
     * Opened by a server, being edited.
     */
    EDITING,
    /**
     * Marked as done in the editor; the server may pull it. Editing is still possible and submits again.
     */
    SUBMITTED
}
