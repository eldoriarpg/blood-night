/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import net.strokkur.commands.CustomSuggestion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Suggests the names of the server's worlds for a command argument.
 */
@CustomSuggestion
@Retention(RetentionPolicy.SOURCE)
public @interface WorldSuggestions {}
