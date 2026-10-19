/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items.look;

import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.Arrays;
import java.util.Locale;

/**
 * Where an item has to be for an attribute modifier of it to count, as Minecraft groups equipment slots.
 */
public enum ItemSlot {
    ANY,
    MAINHAND,
    OFFHAND,
    HAND,
    HEAD,
    CHEST,
    LEGS,
    FEET,
    ARMOR,
    BODY,
    SADDLE;

    /**
     * @return Minecraft's group of slots
     */
    public EquipmentSlotGroup group() {
        EquipmentSlotGroup group = EquipmentSlotGroup.getByName(name().toLowerCase(Locale.ROOT));
        return group == null ? EquipmentSlotGroup.ANY : group;
    }

    /**
     * @param group Minecraft's group of slots
     * @return the slot of that group, {@link #ANY} for one this plugin does not know
     */
    public static ItemSlot of(EquipmentSlotGroup group) {
        return Arrays.stream(values())
                .filter(slot -> slot.name().equalsIgnoreCase(group.toString()))
                .findFirst()
                .orElse(ANY);
    }
}
