/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor.session;

import de.eldoria.bloodnight.web.api.editor.EditorArea;

import java.util.Set;

/**
 * Lets another person edit a session.
 *
 * @param name  their name, e.g. the player's name
 * @param areas what they may change
 */
public record AddParticipant(String name, Set<EditorArea> areas) {}
