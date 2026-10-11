/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor;

/**
 * A participant's access to a session.
 *
 * @param name      their name
 * @param editorUrl their personal link to edit the session
 */
public record ParticipantCreated(String name, String editorUrl) {}
