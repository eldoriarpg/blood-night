/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor;

import tools.jackson.databind.JsonNode;

import java.util.Set;

/**
 * Opens an editor session with a server's data.
 *
 * @param catalog     version of the catalog the server runs; the catalog must be uploaded first unless the service
 *                    has it
 * @param payload     the server's editor payload
 * @param participant name of who opens it, e.g. the player's name; they become the first participant
 * @param areas       what they may change
 */
public record CreateSession(String catalog, JsonNode payload, String participant, Set<EditorArea> areas) {}
