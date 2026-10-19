/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.settings;

import net.strokkur.commands.CustomSuggestion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Suggests the setting paths of the target typed before, see {@link SettingsTargets}.
 */
@CustomSuggestion
@Retention(RetentionPolicy.SOURCE)
public @interface SettingsPathSuggestions {}
