/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.meta;

import org.bukkit.attribute.AttributeModifier.Operation;

/**
 * A change to one attribute of a mob, e.g. {@code minecraft:movement_speed} by {@code 0.2} with
 * {@code ADD_SCALAR} for 20 % faster.
 *
 * @param attribute the attribute's key
 * @param operation how the amount applies: {@code ADD_NUMBER} adds to the base, {@code ADD_SCALAR} adds a share of
 *                  the base, {@code MULTIPLY_SCALAR_1} multiplies the total by one plus the amount
 * @param amount    the amount
 */
public record AttributeChange(String attribute, Operation operation, double amount) {}
