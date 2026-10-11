/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.api;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;

/**
 * An editor session as the editor and the pulling server see it.
 *
 * @param id       the session's id
 * @param state    where it stands
 * @param catalog  version of the catalog the server runs
 * @param payload  the payload as last saved
 * @param problems what the service found wrong with it, best effort
 * @param expires  when the session is deleted unless it is used again
 */
public record SessionView(
        String id,
        SessionState state,
        String catalog,
        JsonNode payload,
        List<PayloadProblem> problems,
        Instant expires) {}
