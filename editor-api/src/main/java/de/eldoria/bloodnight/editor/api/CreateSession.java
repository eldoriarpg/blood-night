/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.api;

import tools.jackson.databind.JsonNode;

/**
 * Opens an editor session with a server's data.
 *
 * @param catalog version of the catalog the server runs; the catalog must be uploaded first unless the service
 *                has it
 * @param payload the server's editor payload
 */
public record CreateSession(String catalog, JsonNode payload) {}
