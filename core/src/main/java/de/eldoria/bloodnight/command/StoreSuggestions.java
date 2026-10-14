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
 * Suggests the packs and mobs of the store, see {@link StoreIds}.
 */
@CustomSuggestion
@Retention(RetentionPolicy.SOURCE)
public @interface StoreSuggestions {}
