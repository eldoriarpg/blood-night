/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.util;

/**
 * Commands from the settings, as players type them.
 */
public final class Commands {
    private Commands() {}

    /**
     * The server runs a command only without its leading slash, but players write one out of habit.
     *
     * @return the command without the spaces around it and without a leading slash
     */
    public static String withoutSlash(String command) {
        String trimmed = command.strip();
        return trimmed.startsWith("/") ? trimmed.substring(1).strip() : trimmed;
    }
}
