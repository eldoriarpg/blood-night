/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor.stream;

/**
 * Takes or releases a section.
 *
 * @param section the section, see {@link LockInfo}
 */
public record LockRequest(String section) {}
