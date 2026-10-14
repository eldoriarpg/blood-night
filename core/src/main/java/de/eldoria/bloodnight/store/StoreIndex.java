/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import java.util.List;

/**
 * A store's {@code index.json}: what it is, its packs and the ids of its mob packages ({@code <id>.json} beside it).
 *
 * @param format  the store format, {@value StoreFiles#FORMAT} for this one
 * @param id      the store's id, e.g. {@code bloodnight}
 * @param packs   its packs
 * @param entries the ids of its mobs
 */
public record StoreIndex(int format, String id, List<StorePack> packs, List<String> entries) {}
