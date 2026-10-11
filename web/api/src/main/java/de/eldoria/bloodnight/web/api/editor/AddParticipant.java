/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor;

/**
 * Lets another person edit a session.
 *
 * @param name their name, e.g. the player's name
 */
public record AddParticipant(String name) {}
