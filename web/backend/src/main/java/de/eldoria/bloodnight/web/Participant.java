/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

/**
 * Someone allowed to edit a session.
 *
 * @param tokenHash the hash of their participant token, which identifies them
 * @param name      their name, shown to the others
 */
record Participant(String tokenHash, String name) {}
