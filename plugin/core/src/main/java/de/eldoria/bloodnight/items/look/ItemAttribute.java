/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items.look;

import org.bukkit.attribute.AttributeModifier.Operation;

import java.util.Objects;

/**
 * An attribute modifier an item gives whoever holds or wears it, e.g. +3 attack damage in the main hand.
 *
 * @param attribute the attribute's key, e.g. {@code minecraft:attack_damage}
 * @param amount    how much it changes the attribute
 * @param operation how the amount is applied: added, added as a share of the base, or multiplied
 * @param slot      where the item has to be for it to count
 */
public record ItemAttribute(String attribute, double amount, Operation operation, ItemSlot slot) {
    public ItemAttribute {
        Objects.requireNonNull(attribute, "attribute");
        operation = Objects.requireNonNullElse(operation, Operation.ADD_NUMBER);
        slot = Objects.requireNonNullElse(slot, ItemSlot.ANY);
    }
}
