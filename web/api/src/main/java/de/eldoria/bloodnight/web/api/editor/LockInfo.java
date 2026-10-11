/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web.api.editor;

/**
 * A section of the payload held by a participant; others cannot change it until it is released.
 * <p>
 * Sections are {@code mobs/<id>}, {@code macros/<id>}, {@code settings/main}, {@code settings/globalDrops} and
 * {@code settings/worlds/<uid>}.
 *
 * @param section the section
 * @param holder  the name of the participant holding it
 */
public record LockInfo(String section, String holder) {}
