/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor.session;

import java.time.Instant;

/**
 * An opened editor session.
 *
 * @param id        the session's id
 * @param editorUrl the opener's personal link to edit the session
 * @param pullToken the secret the server pulls the result, adds participants and closes the session with
 * @param expires   when the session is deleted unless it is used again
 */
public record SessionCreated(String id, String editorUrl, String pullToken, Instant expires) {}
