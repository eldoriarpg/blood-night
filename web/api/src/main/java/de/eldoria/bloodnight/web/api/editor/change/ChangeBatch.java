/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor.change;

import de.eldoria.bloodnight.web.api.editor.stream.LockInfo;
import tools.jackson.databind.JsonNode;

/**
 * Changes the editor made to a session's payload, saved as one revision on top of the latest one.
 *
 * @param operations JSON Patch operations (RFC 6902) on the payload, applied in order, all or none; they may only
 *                   touch sections that are free or held by the sender, see {@link LockInfo}
 */
public record ChangeBatch(JsonNode operations) {}
