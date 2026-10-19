/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor.change;

import java.util.List;

/**
 * A saved revision.
 *
 * @param revision the session's revision now
 * @param problems what the service found wrong with the payload now, best effort
 */
public record ChangeAccepted(int revision, List<PayloadProblem> problems) {}
