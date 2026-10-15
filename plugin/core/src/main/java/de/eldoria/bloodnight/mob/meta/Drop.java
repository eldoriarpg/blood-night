/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

/**
 * One entry of a drop table.
 *
 * @param itemId the item: a registered or editor-made item id, or a material key such as {@code minecraft:bone}
 * @param amount how many of it drop at once
 * @param weight how likely it is picked against the other entries of the tables in use
 */
public record Drop(String itemId, int amount, int weight) {}
