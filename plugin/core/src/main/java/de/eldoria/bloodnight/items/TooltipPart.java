/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items;

import org.bukkit.inventory.ItemFlag;

/**
 * A part of an item's tooltip that can be left out; the item keeps what it stands for.
 */
public enum TooltipPart {
    ENCHANTMENTS(ItemFlag.HIDE_ENCHANTS),
    ATTRIBUTES(ItemFlag.HIDE_ATTRIBUTES),
    UNBREAKABLE(ItemFlag.HIDE_UNBREAKABLE),
    DYE(ItemFlag.HIDE_DYE),
    TRIM(ItemFlag.HIDE_ARMOR_TRIM);

    private final ItemFlag flag;

    TooltipPart(ItemFlag flag) {
        this.flag = flag;
    }

    /**
     * @return the flag that hides it
     */
    public ItemFlag flag() {
        return flag;
    }
}
