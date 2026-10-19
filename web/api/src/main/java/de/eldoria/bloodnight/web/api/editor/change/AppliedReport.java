/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor.change;

import java.util.Map;

/**
 * What the server tells the service after applying a session.
 *
 * @param revision the revision it applied
 * @param hashes   its files' hashes after the apply, by file; the next apply checks the files against these
 */
public record AppliedReport(int revision, Map<String, String> hashes) {}
