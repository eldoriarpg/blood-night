/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import java.util.Map;

/**
 * A group of store entries installed together.
 *
 * @param id           the pack's id, e.g. {@code classic}
 * @param names        the pack's name by language, e.g. {@code en}
 * @param descriptions what the pack holds, by language
 */
public record StorePack(String id, Map<String, String> names, Map<String, String> descriptions) {}
