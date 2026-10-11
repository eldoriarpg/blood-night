/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.api;

/**
 * Something wrong with a payload.
 *
 * @param path    where, as a JSON pointer, e.g. {@code /mobs/speed_zombie/behaviour/nodes/add}
 * @param message what is wrong
 */
public record PayloadProblem(String path, String message) {}
