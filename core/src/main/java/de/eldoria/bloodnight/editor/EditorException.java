/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

/**
 * The editor service refused a request or could not be reached.
 */
public final class EditorException extends RuntimeException {
    /**
     * @param message what went wrong, readable for an admin
     */
    public EditorException(String message) {
        super(message);
    }
}
