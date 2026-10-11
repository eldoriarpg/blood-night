/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.api;

import java.time.Instant;

/**
 * An opened editor session.
 *
 * @param id        the session's id; whoever knows it can edit the session
 * @param editorUrl where the session is edited
 * @param pullToken the secret the server pulls the result and closes the session with
 * @param expires   when the session is deleted unless it is used again
 */
public record SessionCreated(String id, String editorUrl, String pullToken, Instant expires) {}
