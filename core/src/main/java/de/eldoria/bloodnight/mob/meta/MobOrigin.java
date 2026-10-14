/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

/**
 * Where an installed mob came from: the store entry and the hash of the logic it brought. The current logic hashing
 * to the same value means nobody changed it since; a store entry with another hash is a newer version.
 *
 * @param store the store's id, e.g. {@code bloodnight}
 * @param entry the entry's id in that store
 * @param hash  the hash of the behaviour and extension as installed
 */
public record MobOrigin(String store, String entry, String hash) {}
